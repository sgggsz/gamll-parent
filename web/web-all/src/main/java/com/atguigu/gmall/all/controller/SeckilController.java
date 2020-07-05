package com.atguigu.gmall.all.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.seckill.client.SeckillFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 秒杀
 */
@Controller
public class SeckilController {

    @Autowired
    SeckillFeignClient seckillFeignClient;

    @GetMapping("seckill.html")
    public String index(Model model) {
        Result result = seckillFeignClient.findAll();
        model.addAttribute("list", result.getData());
        return "seckill/index";
    }


    @GetMapping("seckill/{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) {
        //通过skuId 查询skuInfo
        Result result = seckillFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item", result.getData());
        return "seckill/item";
    }

    //进入秒杀页面
    @GetMapping("seckill/queue.html")
    public String queue(@RequestParam(name = "skuId") Long skuId,
                        @RequestParam(name = "skuIdStr") String skuIdStr,
                        HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String skuIdStrForService = MD5.encrypt(userId);
        if(!skuIdStrForService.equals(skuIdStr)){
            return "seckill/fail";
        }else{
            request.setAttribute("skuId", skuId);
            request.setAttribute("skuIdStr", skuIdStr);
            return "seckill/queue";
        }
    }


    //生成订单页面
    @GetMapping("seckill/trade.html")
    public String trade(Model model) {
        Result<Map<String, Object>> result = seckillFeignClient.trade();
        if(result.isOk()) {
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        } else {
            model.addAttribute("message",result.getMessage());

            return "seckill/fail";
        }
    }


}
