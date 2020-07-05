package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * ConfirmReceiver
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-29
 * @Description:
 */
@Component
@Configuration
public class ConfirmReceiver {

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm", autoDelete = "true"),
            exchange = @Exchange(value = "exchange.confirm", autoDelete = "true"),
            key = {"routing.confirm11"})
    )
    public void process(Message message, Channel channel){
        System.out.println("消息接受成功到达消费端");
    }
}