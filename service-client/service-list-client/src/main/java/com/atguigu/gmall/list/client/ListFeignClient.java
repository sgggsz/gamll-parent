package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(value = "service-list")
public interface ListFeignClient {

    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId);

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId);

    /**
     * 商品平台属性回显
     * @param searchParam
     * @return
     */
    @RequestMapping("api/list/list")
    Result<Map> list(@RequestBody SearchParam searchParam);

    //为商品增加热度值
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    void incrHotScore(@PathVariable("skuId")Long skuId);
}
