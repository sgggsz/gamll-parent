package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * RabbitService
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-29
 * @Description:
 */
@Service
public class RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    public boolean sendMessage(String exchange,String routingKey,Object message){
        // 封装一个CorrelationData类型的对象，放入缓存
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String correlationId = UUID.randomUUID().toString();
        gmallCorrelationData.setId(correlationId);
        gmallCorrelationData.setMessage(message);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);

        // 保存原始消息文本和id到缓存，方便后面消息出现问题的处理
        String str = JSON.toJSONString(gmallCorrelationData);
        redisTemplate.opsForValue().set(gmallCorrelationData.getId(), str);// 直接放，不用转化json

        // 使用携带CorrelationData有消息id的队列发送
        rabbitTemplate.convertAndSend(exchange,routingKey,message,gmallCorrelationData);

        return true;
    }
}