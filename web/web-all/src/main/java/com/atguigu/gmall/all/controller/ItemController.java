package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ItemFeignClient;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * ItemController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-05
 * @Description:
 */
@RequestMapping
@Controller
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;



    /**
     * sku详情页面
     * @param skuId
     * @param model
     * @return@PathVariable Long skuId, Model model
     */
    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) {
        // map中包含，skuInfo，List<SpuSaleAttr>，price，BaseCategoryView
        Result<Map> result = itemFeignClient.getItem(skuId);

        // 批量想域中放置map
        model.addAllAttributes(result.getData());

        // modelMap.put("key","value");
        return "item/index";
    }





    @RequestMapping("testItem")
    @ResponseBody
    public String testItem(){

        // 通过feign调用service-item
        Result item = itemFeignClient.testItemApi();


        return "itemController";
    }

    @RequestMapping("a")
    public String a(ModelMap modelMap){
        String hello = "hello thymeleaf";
        modelMap.put("hello",hello);

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("元素"+ i);
        }
        modelMap.put("list",list);
        modelMap.put("che","1");
        return "test";
    }
}