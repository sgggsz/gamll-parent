package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PayService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PayServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-28
 * @Description:
 */
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    RabbitService rabbitService;


    /**
     * 支付成功，更新订单状态
     * @param paymentInfo
     */
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",paymentInfo.getOutTradeNo());
        paymentInfoMapper.update(paymentInfo,wrapper);
    }


    @Override
    public String paySubmit(String orderId) {

        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址
        // 封装业务参数
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",orderInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        orderInfo.sumTotalAmount();
        map.put("total_amount",0.01);
        map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
        alipayRequest.setBizContent(JSON.toJSONString(map));


        String form= "" ;
        try  {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        }  catch  (AlipayApiException e) {
            e.printStackTrace();
        }

        // 将支付信息保存起来
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(Long.parseLong(orderId));
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setPaymentType(PaymentType.ALIPAY.getComment());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        savePaymentInfo(paymentInfo);
        return form;
    }


    public void savePaymentInfo(PaymentInfo paymentInfo) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",paymentInfo.getOutTradeNo());
        paymentInfoMapper.update(paymentInfo,queryWrapper);

        // 传递orderId
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(queryWrapper);

        // 发送支付成功的队列，让订单消费修改订单状态
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,paymentInfo1.getOrderId());

    }
}