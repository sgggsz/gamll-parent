package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;

/**
 * MqController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-29
 * @Description:
 */
@RestController
@RequestMapping("/mq")
@Slf4j
public class MqApiController {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("sendConfirm")
    public Result sendConfirm(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rabbitService.sendMessage("exchange.confirm","routing.confirm",sdf);
        return Result.ok();
    }

}