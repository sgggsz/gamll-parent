package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

public interface PayService {
    String paySubmit(String orderId);

    void updatePayment(PaymentInfo paymentInfo);
}
