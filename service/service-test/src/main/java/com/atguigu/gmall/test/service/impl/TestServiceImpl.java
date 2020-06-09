package com.atguigu.gmall.test.service.impl;

import com.atguigu.gmall.test.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;


}
