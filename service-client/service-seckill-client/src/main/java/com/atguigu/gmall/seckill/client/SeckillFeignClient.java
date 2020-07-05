package com.atguigu.gmall.seckill.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * SeckillFeignClient
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-07-02
 * @Description:
 */
@FeignClient("service-activity")
public interface SeckillFeignClient {


    @RequestMapping("api/activity/seckill/findAll")
    Result findAll();

    @RequestMapping("api/activity/seckill/getSeckillGoods/{skuId}")
    Result getSeckillGoods(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/activity/seckill/auth/trade")
    Result<Map<String,Object>> trade();
}