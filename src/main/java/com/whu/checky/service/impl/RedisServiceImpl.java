package com.whu.checky.service.impl;

import com.whu.checky.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service("redisService")
public class RedisServiceImpl implements RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String checkSessionId(String sessionId) {
        return stringRedisTemplate.opsForValue().get(sessionId);
    }

    @Override
    public void saveSessionId(String sessionId, String username) {
        stringRedisTemplate.opsForValue().set(sessionId,username);
    }
}
