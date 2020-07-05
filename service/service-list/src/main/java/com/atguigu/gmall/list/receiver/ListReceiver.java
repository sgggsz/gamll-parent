package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ListReceiver
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-30
 * @Description:
 */
@Component
public class ListReceiver {

    @Autowired
    SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS, type = ExchangeTypes.DIRECT, durable = "true"),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoods(Long skuId, Message message, Channel channel) throws IOException {
        try {
            //上架
            searchService.upperGoods(skuId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (AmqpException Exception) {
            // 消费失败，消息回滚
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS, type = ExchangeTypes.DIRECT, durable = "true"),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoods(Long skuId, Message message, Channel channel) throws IOException {
        try {
            //下架
            searchService.lowerGoods(skuId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (AmqpException Exception) {
            // 消费失败，消息回滚
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}