package com.tongxin.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Redis 分布式锁服务
 * 基于 SET NX PX 实现，value 存唯一标识防止误删他人锁，释放用 Lua 脚本保证原子性。
 * 典型用途：预约号源并发控制（防超卖）、热点数据重建互斥。
 *
 * @author wyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final StringRedisTemplate stringRedisTemplate;

    /** 释放锁的 Lua 脚本：只有 value 匹配才删除，保证原子性 */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final DefaultRedisScript<Long> unlockScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁 key
     * @param ttlSeconds 锁过期时间（秒），防止死锁
     * @return 锁标识（获取成功时非 null，用于释放）；获取失败返回 null
     */
    public String tryLock(String lockKey, long ttlSeconds) {
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));
        if (Boolean.TRUE.equals(acquired)) {
            return lockValue;
        }
        return null;
    }

    /**
     * 释放锁
     *
     * @param lockKey   锁 key
     * @param lockValue 加锁时返回的标识
     */
    public void unlock(String lockKey, String lockValue) {
        if (lockValue == null) return;
        try {
            stringRedisTemplate.execute(unlockScript, List.of(lockKey), lockValue);
        } catch (Exception e) {
            log.warn("释放 Redis 锁失败: lockKey={}", lockKey, e);
        }
    }

    /**
     * 带锁执行业务逻辑（便捷方法）
     * 获取失败直接抛 BusinessException，调用方无需手动释放。
     *
     * @param lockKey    锁 key
     * @param ttlSeconds 锁过期时间
     * @param failMsg    获取失败时的异常消息
     * @param action     要执行的业务逻辑
     * @return 业务逻辑返回值
     */
    public <T> T executeWithLock(String lockKey, long ttlSeconds, String failMsg, Supplier<T> action) {
        String lockValue = tryLock(lockKey, ttlSeconds);
        if (lockValue == null) {
            throw new com.tongxin.ai.common.BusinessException(failMsg);
        }
        try {
            return action.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }
}
