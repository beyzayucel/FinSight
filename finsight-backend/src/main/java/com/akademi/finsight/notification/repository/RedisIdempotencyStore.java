package com.akademi.finsight.notification.repository;

import com.akademi.finsight.notification.config.NotificationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/** SET key NX EX ttl tek komutta hem atomik acquire hem otomatik temizlik saglar. */
@Repository
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "notification:idempotency:";

    private final StringRedisTemplate redisTemplate;
    private final NotificationProperties properties;

    public RedisIdempotencyStore(StringRedisTemplate redisTemplate, NotificationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public boolean tryAcquire(String eventId) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key(eventId), "1", properties.idempotency().ttl()));
    }

    @Override
    public void release(String eventId) {
        redisTemplate.delete(key(eventId));
    }

    private String key(String eventId) {
        return KEY_PREFIX + eventId;
    }
}
