package com.atguigu.gmall.payment.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.payment.PaymentInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * a
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-28
 * @Description:
 */
@FeignClient("service-payment")
public interface PaymentFeignClient {

    @RequestMapping("api/payment/inner/updatePayment")
    Result updatePayment(@RequestBody PaymentInfo paymentInfo);
}