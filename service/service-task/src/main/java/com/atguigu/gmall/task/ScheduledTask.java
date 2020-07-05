package com.atguigu.gmall.task;


import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class ScheduledTask {


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //定时开启秒杀任务（入库）
    @Scheduled(cron = "0 22 17 * * ?")
    public void task() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_1,"11");
    }


    //消息补偿机制定时任务
//    @Scheduled(cron = "0/30 * * * * ?")
//    public void task() {
//        String correlationId = (String)redisTemplate.opsForList().rightPop(MqConst.MQ_KEY_PREFIX);
//        // 每个X秒查看redis缓存中的mq:list补偿消息列表，查询出需要补偿得消息，发送mq进行二次消费
//        correlationId = correlationId.replace("\\\"", "");
//        if(!StringUtils.isEmpty(correlationId))
//        {
//            String gmallCorrelationDataStr = (String)redisTemplate.opsForValue().get(correlationId);
//            GmallCorrelationData gmallCorrelationData = JSON.parseObject(gmallCorrelationDataStr, GmallCorrelationData.class);
//
//            String exchange = gmallCorrelationData.getExchange();
//            String routingKey = gmallCorrelationData.getRoutingKey();
//            Object message = gmallCorrelationData.getMessage();
//            rabbitTemplate.convertAndSend(exchange,routingKey,message,gmallCorrelationData);
//        }
//
//    }
}
