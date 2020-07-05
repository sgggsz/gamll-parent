package com.atguigu.gmall.seckill.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * SeckillReceive
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-07-02
 * @Description:
 */
@Component
public class SeckillReceive {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    RedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK, type = ExchangeTypes.DIRECT, durable = "true"),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importItemToRedis(Message message, Channel channel) throws IOException {

        //将秒杀商品从数据库放入缓存.mysql-->redis
        List<SeckillGoods> seckillGoods = seckillGoodsService.selectList();
        for (SeckillGoods seckillGood : seckillGoods) {
            redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS,seckillGood.getSkuId()+"",seckillGood);

            // 增加库存
            Integer stockCount = seckillGood.getStockCount();
            for (int i = 0; i < stockCount; i++) {
                redisTemplate.opsForList().leftPush(RedisConst.SECKILL_GOODS+":"+seckillGood.getSkuId()+"",seckillGood.getSkuId());
            }

            // 发布redis的订阅消息队列，通知各个服务器，商品已经发布
            redisTemplate.convertAndSend("seckillpush",seckillGood.getSkuId()+":1");
        }


    }

    //mq收到秒杀信息之后
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER, type = ExchangeTypes.DIRECT, durable = "true"),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckillOrder(UserRecode userRecode, Message message, Channel channel) throws IOException {
        seckillGoodsService.seckillOrder(userRecode);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }
}