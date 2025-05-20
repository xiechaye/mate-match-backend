package com.suave.matematch.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RedisUtils {
    private static RedisTemplate<String, Object> redisTemplate;

    @Resource
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

    public static <T> List<T> get(String key) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            return (List<T>) valueOperations.get(key);
        } catch (ClassCastException e) {
            log.error("类型转换异常: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Redis读取异常: {}", e.getMessage());
            return null;
        }
    }

    public static <T> void set(String key, List<T> value) {
        try {
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value);
        } catch (Exception e) {
            log.error("Redis写入异常: {}", e.getMessage());
        }
    }

    public static void del(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis删除异常: {}", e.getMessage());
        }
    }
}
