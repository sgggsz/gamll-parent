package com.atguigu.gmall.product.client;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-item")
public interface ItemFeignClient {



    /**
     * @return
     */
    @GetMapping("/api/item/{skuId}")
    Result getItem(@PathVariable("skuId") Long skuId);





    @GetMapping("/api/item/testItemApi")
    Result testItemApi();

}
