package com.atguigu.gmall.item.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ItemApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-05
 * @Description:
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    ItemService itemService;

    @RequestMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId){

        // 调用item的服务获得数据返回结果，item的服务其实是product的数据汇总
        Map<String, Object> map = itemService.getSkuById(skuId);
        return Result.ok(map);
    }

    @RequestMapping("testItemApi")
    public Result testItemApi(){
        return Result.ok("itemApiController");
    }

    @GetMapping("testItemApi")
    Result testItemApi11(){
        return Result.ok("1111");
    }

}