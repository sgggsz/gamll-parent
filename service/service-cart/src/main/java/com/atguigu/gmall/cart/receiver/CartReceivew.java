package com.atguigu.gmall.cart.receiver;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * CartReceivew
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-07-05
 * @Description:
 */
@Component
public class CartReceivew {

    @Autowired
    CartInfoService cartInfoService;

    //mq合并购物车消息之后
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart.queue", durable = "true"),
            exchange = @Exchange(value = "cart", durable = "true"),
            key = {"C"}
    ))
    public void mergeCart(Map<String,String> map, Message message, Channel channel) throws IOException {
        String userId = map.get("userId");
        String userTempId = map.get("userTempId");

        //合并购物车
        cartInfoService.mergeCart(userId,userTempId);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}