package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @Override
    public Map<String, Object> getSkuById(Long skuId) {
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