package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PayApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-28
 * @Description:
 */
@RestController
@RequestMapping("api/payment")
public class PayApiController {

    @Autowired
    PayService payService;

    @RequestMapping("inner/updatePayment")
    Result updatePayment(@RequestBody PaymentInfo paymentInfo){

        payService.updatePayment(paymentInfo);

        return Result.ok();

    }

    @RequestMapping("alipay/submit/{orderId}")
    public String alipaySubmit(@PathVariable String orderId){
        String form = payService.paySubmit(orderId);

        return form;
    }

}