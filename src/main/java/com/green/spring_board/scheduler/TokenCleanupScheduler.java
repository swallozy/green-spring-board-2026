package com.green.spring_board.scheduler;

import com.green.spring_board.repository.AccessTokenBlacklistRepository;
import com.green.spring_board.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Component
@AllArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Transactional
    // 초 분 시 일 월 요일
    // 매 단위에 실행할 것이다 -> *
    // 특정 값에 실행할 것이다 -> 1, 2, 3 등 상수
    // 특정 값 기준 일정 주기로 실행할 것이다 -> 기준점/주기 (ex 0/10)

    // 10 * * * * * -> 항상 매분의 10초마다 실행
    // 0/10 * * * * * -> 항상 10초마다 실행 (10초에 한 번)

    // 매일 오전 3시에 만료된 블랙 리스트의 액세스 토큰과 만료된 리프레시 토근을 삭제한느 스케줄러
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.warn("토큰 정리 스케줄러가 실행됩니다. 실행 시작 시간 : {}", now);

        // 1. RefreshToken 테이블에서 만료 시간이 지난 리프레시 토큰들을 찾아서 제거
        refreshTokenRepository.deleteAllByExpirationDatetimeBefore(now);

        // 2. AccessTokenBlacklist 테이블에서 만료 시간이 지난 액세스 토큰들을 찾아서 제거
        accessTokenBlacklistRepository.deleteAllByExpirationDatetimeBefore(now);
        log.warn("토큰 정리 스케줄러가 작업이 성공적으로 마무리 되었습니다.");

    }
}