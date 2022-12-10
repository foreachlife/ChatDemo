package com.example.chatdemo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author yy
 * @data 2022/12/9 22:15
 */
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void convertAndSend(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public Set sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public void sDel(String key, String userId) {
        redisTemplate.opsForSet().remove(key, userId);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void sSet(String rooms, String value1, String value2) {

        redisTemplate.opsForSet().add(rooms, value1, value2);
    }

    public void sSet(String rooms, String value1) {

        redisTemplate.opsForSet().add(rooms, value1);
    }
}
