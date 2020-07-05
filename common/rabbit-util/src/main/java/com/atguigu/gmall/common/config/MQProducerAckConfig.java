package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.entity.GmallCorrelationData;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * MQProducerAckConfig
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-29
 * @Description:
 */
@Component
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback ,RabbitTemplate.ReturnCallback{


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);            //指定 ConfirmCallback
        rabbitTemplate.setReturnCallback(this);             //指定 ReturnCallback
    }

    /**
     * 发送消息到交换机后调用
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        if (ack){
            System.out.println("消息发送和投递成功");
        }else {
            System.out.println("消息发送失败");
            // 消息发送失败，从缓存中取出原始文本
            String correlationId = correlationData.getId();
            String gmallCorrelationDataStr = (String)redisTemplate.opsForValue().get(correlationId);// 消息发送时的原始文本
            GmallCorrelationData gmallCorrelationData = JSON.parseObject(gmallCorrelationDataStr, GmallCorrelationData.class);
            addRetry(gmallCorrelationData);
        }

    }

    /**
     *投递消息到队列时失败回调(无人消费)
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        // 反序列化对象输出
        System.out.println("消息主体: "+ new String(message.getBody()));
        System.out.println("应答码: "+ replyCode);
        System.out.println("描述："+ replyText);
        System.out.println("消息使用的交换器 exchange : "+ exchange);
        System.out.println("消息使用的路由键 routing : "+ routingKey);

        // 如果投递失败，说明消息没有发出去，记录缓存，后期定时任务处理
        String correlationId =  (String)message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");
        String gmallCorrelationDataStr = (String)redisTemplate.opsForValue().get(correlationId);// 消息发送时的原始文本
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(gmallCorrelationDataStr, GmallCorrelationData.class);
        addRetry(gmallCorrelationData);

    }

    private void addRetry(GmallCorrelationData gmallCorrelationData) {

        int retryCount = gmallCorrelationData.getRetryCount();
        retryCount++;
        gmallCorrelationData.setRetryCount(retryCount);
        if(retryCount<=3){
            redisTemplate.opsForList().leftPush(MqConst.MQ_KEY_PREFIX, JSON.toJSONString(gmallCorrelationData.getId()));// 放入需要补偿消费得消息得id
            //次数更新
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(), JSON.toJSONString(gmallCorrelationData));
        }

    }
}