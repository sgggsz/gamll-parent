package com.atguigu.gmall.order.client;

import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OrderFeignClient
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-27
 * @Description:
 */
@FeignClient("service-order")
public interface OrderFeignClient {

    @RequestMapping("api/order/inner/getTradeNo/{userId}")
    public String getTradeNo(@PathVariable("userId") String userId);

    @PostMapping("api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable("orderId") String orderId);

    /**
     * 提交秒杀订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo);
}