package com.zia.payments.point.service;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
import com.zia.payments.idempotency.domain.IdempotencyRequest;
import com.zia.payments.idempotency.domain.IdempotencyStatus;
import com.zia.payments.idempotency.service.IdempotencyService;
import com.zia.payments.point.domain.PointLedger;
import com.zia.payments.point.domain.PointWallet;
import com.zia.payments.point.dto.response.ChargeResponse;
import com.zia.payments.point.dto.response.RedeemResponse;
import com.zia.payments.point.repository.PointLedgerRepository;
import com.zia.payments.point.repository.PointWalletRepository;
import com.zia.payments.user.domain.User;
import com.zia.payments.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointWalletRepository pointWalletRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final UserRepository userRepository;
    private final IdempotencyService idempotencyService;

    /**
     * 포인트 충전
     * @param userId 유저 ID
     * @param amount 충전 금액 (양수만 가능)
     * @param memo 메모
     * @return 충전 후 지갑 정보 DTO
     */
    @Override
    @Transactional
    public ChargeResponse charge(Long userId, Long amount, String memo) {

        // 충전 금액 검증
        if(amount == null || amount <= 0) {
            throw new ApiException(ErrorCode.INVALID_AMOUNT);
        }

        // 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 지갑 조회 또는 생성 (비관적 락 적용)
        PointWallet wallet = pointWalletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.WALLET_NOT_FOUND));

        // 포인트 증가
        wallet.increase(amount);

        // 지갑 저장
        pointWalletRepository.save(wallet);

        // 원장 기록
        PointLedger ledger = PointLedger.charge(user, amount, wallet.getBalance(), memo);
        pointLedgerRepository.save(ledger);

        log.info("포인트 충전 성공 : userId={}, amount={}, balanceAfter={}", userId, amount, wallet.getBalance());

        // DTO 변환해서 반환
        return ChargeResponse.builder()
                .userId(userId)
                .chargedAmount(amount)
                .balanceAfter(wallet.getBalance())
                .memo(memo)
                .build();
    }

    /**
     * 포인트 잔액 조회
     * @param userId
     * @return 현재 잔액 (없으면 0)
     */
    @Override
    @Transactional(readOnly = true)
    public Long getBalance(Long userId) {
        return pointWalletRepository.findByUserId(userId)
                .map(PointWallet::getBalance)
                .orElse(0L);
    }

    // 새로운 지갑 생성 헬퍼 메서드
    private PointWallet createNewWallet(User user) {
        PointWallet newWallet = PointWallet.builder()
                .user(user)
                .balance(0L)
                .build();
        PointWallet savedWallet = pointWalletRepository.save(newWallet);
        log.info("새 지갑 생성 : userId={}", user.getId());
        return savedWallet;
    }

    /**
     * 포인트 차감 (동시성 제어, 멱등성 포함)
     * @param userId
     * @param amount 차감 금액 (양수만 가능)
     * @param memo 메모
     * @param requestId 멱등키
     * @return 차감 결과 DTO
     */
    @Override
    @Transactional
    public RedeemResponse redeem(Long userId, Long amount, String memo, String requestId) {

        // requestId 검증
        if(requestId == null || requestId.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "requestId는 필수입니다.");
        }

        // 멱등키 조회 (SUCCESS면 캐시 반환)
        IdempotencyRequest idempotencyRequest = idempotencyService.findByRequestId(requestId);
        if(idempotencyRequest != null && idempotencyRequest.getIdempotencyStatus() == IdempotencyStatus.SUCCESS) {
            log.info("멱등성 캐시 히트 : requestId={}, userId={}", requestId, userId);
            return parseRedeemResponse(idempotencyRequest.getResponseBody());
        }

        // IN_PROGRESS로 선점
        idempotencyService.createInProgress(userId, requestId, "/api/users/{userId}/points/redeem");

        try {
            // 실제 차감 로직
            RedeemResponse response = doRedeem(userId, amount, memo);

            // 성공 저장
            String responseJson = serializeRedeemResponse(response);
            idempotencyService.markSuccess(requestId, responseJson);

            return response;
        } catch (Exception e) {
            // 실패 저장
            idempotencyService.markFailed(requestId);
            throw e;
        }
    }

    /** 포인트 차감 (동시성 X - 테스트용)
     * 동시성 깨지는 케이스 재현 목적
     */
    @Override
    @Transactional
    public RedeemResponse redeemNoLock(Long userId, Long amount, String memo) {
        if(amount == null || amount <= 0) {
            throw new ApiException(ErrorCode.INVALID_AMOUNT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 락 없이 조회
        PointWallet wallet = pointWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.WALLET_NOT_FOUND));

        wallet.decrease(amount);

        pointWalletRepository.save(wallet);

        PointLedger ledger = PointLedger.redeem(user, amount, wallet.getBalance(), memo);
        pointLedgerRepository.save(ledger);

        return RedeemResponse.builder()
                .userId(userId)
                .redeemedAmount(amount)
                .balanceAfter(wallet.getBalance())
                .memo(memo)
                .build();
    }

    // 실제 포인트 차감 (헬퍼 메서드)
    private RedeemResponse doRedeem(Long userId, Long amount, String memo) {
        // amount 검증
        if(amount == null || amount <= 0) {
            throw new ApiException(ErrorCode.INVALID_AMOUNT);
        }

        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 지갑 조회 (비관적 락 적용)
        PointWallet wallet = pointWalletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.WALLET_NOT_FOUND));

        // 포인트 차감 (잔액 부족 체크)
        wallet.decrease(amount);

        // 지갑 저장
        pointWalletRepository.save(wallet);

        // 원장 기록 (redeem)
        PointLedger ledger = PointLedger.redeem(user, amount, wallet.getBalance(), memo);
        pointLedgerRepository.save(ledger);

        log.info("포인트 차감 성공: userId={}, amount={}, balanceAfter={}", userId, amount, wallet.getBalance());

        return RedeemResponse.builder()
                .userId(userId)
                .redeemedAmount(amount)
                .balanceAfter(wallet.getBalance())
                .memo(memo)
                .build();
    }

    // JSON 직렬화 (멱등성용)
    private String serializeRedeemResponse(RedeemResponse response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("RedeemResponse 직렬화 실패 : {}", response, e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // JSON 역직렬화 (멱등성용)
    private RedeemResponse parseRedeemResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, RedeemResponse.class);
        } catch (Exception e) {
            log.error("RedeemResponse 파싱 실패 : {}", json, e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
