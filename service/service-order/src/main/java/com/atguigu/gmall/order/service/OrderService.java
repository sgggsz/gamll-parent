package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderService {
    OrderInfo saveOrderInfo(OrderInfo orderInfo);

    String getTradeNo(String userId);

    boolean checkTradeNo(String userId, String tradeNo);

    OrderInfo getOrderInfo(String orderId);
}
