package com.zia.payments.point.service;

import com.zia.payments.global.exception.ApiException;
import com.zia.payments.point.domain.PointWallet;
import com.zia.payments.point.repository.PointWalletRepository;
import com.zia.payments.user.domain.User;
import com.zia.payments.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class PointServiceConcurrencyTest {

    @Autowired private PointService pointService;
    @Autowired private UserRepository userRepository;
    @Autowired private PointWalletRepository pointWalletRepository;

    // 테스트용 : 초기 잔액을 가진 User + Wallet 생성
    private Long createTestUserWithBalance(Long initialBalance) {
        User user = User.builder()
                .name("Test User")
                .build();
        user = userRepository.save(user);

        PointWallet wallet = PointWallet.builder()
                .user(user)
                .balance(initialBalance)
                .build();
        pointWalletRepository.save(wallet);

        return user.getId();
    }

    @AfterEach
    void tearDown() {
        pointWalletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시 차감: 100개 스레드가 동시에 1000원씩 차감 -> 최종 잔액 0")
    void testConcurrentRedeem_allSuccess() throws Exception {

        Long testUserId = createTestUserWithBalance(100_000L);

        int threadCount = 100;
        long redeemAmount = 1_000L;
        long expectedBalance = 0L;

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch startLatch = new CountDownLatch(1); // 시작
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 완료 대기

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        // 100개 스레드 생성
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await(); // 모두 함께 출발

                    pointService.redeem(testUserId, redeemAmount,"concurrent-redeem");
                    successCount.incrementAndGet();

                } catch (ApiException e) {
                    failureCount.incrementAndGet();
                    log.debug("차감 실패: {}", e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.error("예상치 못한 에러", e);
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        // 모든 스레드 동시 출발
        startLatch.countDown();

        // 모든 스레드 완료 대기
        doneLatch.await();
        executor.shutdown();

        // 스레드 내부 예외 확인
        for (Future<?> f : futures) {
            f.get();
        }

        // 최종 잔액 확인
        Long finalBalance = pointService.getBalance(testUserId);

        log.info("--- 동시성 테스트 결과 ---");
        log.info("성공: {}개, 실패: {}개", successCount.get(), failureCount.get());
        log.info("예상 잔액: {}, 실제 잔액: {}", expectedBalance, finalBalance);

        // 검증
        assertEquals(threadCount, successCount.get());
        assertEquals(0, failureCount.get());
        assertEquals(expectedBalance, finalBalance);
    }

    @Test
    @DisplayName("잔액 부족 : 10,000원인데 100개 스레드가 5,000원씩 -> 2개 성공, 98개 실패")
    void testConcurrentRedeem_insufficientBalance() throws Exception {


        Long testUserIdWithLowBalance = createTestUserWithBalance(10_000L);

        int threadCount = 100;
        long redeemAmount = 5_000L;

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    pointService.redeem(testUserIdWithLowBalance, redeemAmount,"insufficient-test");
                    successCount.incrementAndGet();
                } catch (ApiException e) {
                    failureCount.incrementAndGet();
                    log.debug("차감 실패 (잔액 부족) : {}", e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.error("예상치 못한 에러", e);
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        for (Future<?> f : futures) {
            f.get();
        }
        log.info("잔액 부족 테스트 : 성공 =" + successCount.get() + "개, 실패 =" + failureCount.get() + "개");

        assertEquals(2, successCount.get());
        assertEquals(98, failureCount.get());

    }
}
