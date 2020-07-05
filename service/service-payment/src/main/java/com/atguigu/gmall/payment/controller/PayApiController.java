package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    /**
     // TODO 验签
     * @param sign
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("inner/sign")
    public Result sign(String sign) throws AlipayApiException {
        Map<String, String> paramsMap = null; //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
        }

        return Result.ok();
    }

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