package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ItemServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-07
 * @Description:
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;


    @Override
    public Map<String, Object> getSkuById(Long skuId) {
        Map<String, Object> map = new HashMap<>();

        // 通过skuId 查询skuInfo
        CompletableFuture<SkuInfo> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 保存skuInfo
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        // 销售属性-销售属性值回显并锁定
        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            // 保存数据
            map.put("spuSaleAttrList", spuSaleAttrList);
        },threadPoolExecutor);

            //根据spuId 查询map 集合属性
            // 销售属性-销售属性值回显并锁定
        CompletableFuture<Void> skuValueIdsMapCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());

            String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
            // 保存valuesSkuJson
            map.put("valuesSkuJson", valuesSkuJson);
        }, threadPoolExecutor);

            //分类信息
        CompletableFuture<Void> categoryViewCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView",categoryView);
        },threadPoolExecutor);

        //价格查询
        CompletableFuture<Void> skuPriceCF = CompletableFuture.runAsync(()->{
            // 价格信息
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price",skuPrice);
        },threadPoolExecutor);

        CompletableFuture.allOf(skuCompletableFuture,
                spuSaleAttrCompletableFuture,
                skuValueIdsMapCompletableFuture,
                skuPriceCF,
                categoryViewCompletableFuture).join();
            return map;
    }


    public Map<String, Object> getSkuByIdOld(Long skuId) {
        Map<String, Object> map = new HashMap<>();


        // sku信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        // 分类信息
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

        // 价格信息
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);

        // 页面销售属性列表信息
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());

        //页面销售属性map
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());


        map.put("price",skuPrice);
        map.put("categoryView",categoryView);
        map.put("skuInfo",skuInfo);
        map.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        // 放入销售属性对应skuId的map
        map.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        return map;
    }


}