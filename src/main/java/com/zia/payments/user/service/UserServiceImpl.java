package com.zia.payments.user.service;

import com.zia.payments.point.domain.PointWallet;
import com.zia.payments.point.repository.PointWalletRepository;
import com.zia.payments.user.domain.User;
import com.zia.payments.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PointWalletRepository pointWalletRepository;

    /**
     * 유저 생성 (PointWallet 함께 생성)
     * @param name
     * @return 생성된 유저
     */
    @Override
    public User createUser(String name) {

        // 유저 생성
        User user = User.builder()
                .name(name)
                .build();
        user = userRepository.save(user);

        // 지갑도 함께 생성 (동시성 안전)
        PointWallet wallet = PointWallet.builder()
                .user(user)
                .balance(0L)
                .build();
        pointWalletRepository.save(wallet);

        log.info("사용자 생성 + 지갑 초기화 : userId={}, name={}", user.getId(), name);

        return user;
    }
}
