package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OrderReceiver {

    @Autowired
    OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,durable = "true"),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paySuccess(Long orderId, Message message, Channel channel) throws IOException {

        try{
            OrderInfo orderInfo = orderService.getOrderInfo(orderId+"");
            orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
            orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
            orderService.updateOrderStatus(orderInfo);

            // 发送消息队列给库存
            orderService.sendOrderStatus(orderId);


            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){

            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }



    }

}
