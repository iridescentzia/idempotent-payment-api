package com.zia.payments.point.service;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.global.exception.ErrorCode;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointWalletRepository pointWalletRepository;
    private final PointLedgerRepository pointLedgerRepository;
    private final UserRepository userRepository;

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
     * 포인트 차감 (동시성 제어 포함)
     * @param userId
     * @param amount 차감 금액 (양수만 가능)
     * @param memo 메모
     * @return 차감 결과 DTO
     */
    @Override
    @Transactional
    public RedeemResponse redeem(Long userId, Long amount, String memo) {

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

    /** 포인트 차감 (동시성 X - 테스트용)
     *
     */
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
}
