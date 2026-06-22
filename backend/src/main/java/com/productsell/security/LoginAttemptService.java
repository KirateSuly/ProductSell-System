package com.productsell.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败次数限制服务（内存版）
 * <p>
 * 规则：同一 IP 在 15 分钟内最多允许 5 次失败尝试。
 * 第 6 次起拒绝登录，返回"操作频繁，请稍后再试"。
 * 成功登录后自动清除该 IP 的失败计数。
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000;

    private final ConcurrentHashMap<String, Attempt> store = new ConcurrentHashMap<>();

    /**
     * 检查是否已被锁定
     */
    public boolean isLocked(String ip) {
        Attempt attempt = store.get(ip);
        if (attempt == null) return false;
        if (attempt.count < MAX_ATTEMPTS) return false;
        if (System.currentTimeMillis() - attempt.firstFailTime > LOCK_DURATION_MS) {
            store.remove(ip);
            return false;
        }
        return true;
    }

    /**
     * 获取剩余尝试次数
     */
    public int getRemainingAttempts(String ip) {
        Attempt attempt = store.get(ip);
        if (attempt == null) return MAX_ATTEMPTS;

        if (System.currentTimeMillis() - attempt.firstFailTime > LOCK_DURATION_MS) {
            store.remove(ip);
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempt.count);
    }

    /**
     * 记录一次失败
     */
    public void recordFailure(String ip) {
        store.compute(ip, (k, v) -> {
            if (v == null || System.currentTimeMillis() - v.firstFailTime > LOCK_DURATION_MS) {
                return new Attempt();
            }
            v.count++;
            return v;
        });
        log.warn("IP {} 登录失败，剩余次数: {}", ip, getRemainingAttempts(ip));
    }

    /**
     * 登录成功后清除失败记录
     */
    public void clearAttempts(String ip) {
        store.remove(ip);
    }

    private static class Attempt {
        int count = 1;
        long firstFailTime = System.currentTimeMillis();
    }
}
