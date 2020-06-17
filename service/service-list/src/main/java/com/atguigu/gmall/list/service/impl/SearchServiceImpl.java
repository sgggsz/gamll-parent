package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.Goods;
import com.atguigu.gmall.list.SearchAttr;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * SearchServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-16
 * @Description:
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    ElasticsearchRepository elasticsearchRepository;

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    ProductFeignClient productFeignClient;

    //商品上架功能
    @Override
    public void upperGoods(Long skuId) {

        Goods goods = new Goods();
        //查询goods信息，skuInfo,trademark,category,baseAttrInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo!=null) {
            // 封装商品数据
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setCreateTime(new Date());

            // 查询商标数据
            BaseTrademark baseTrademark = productFeignClient.getTrademarkByTmId(skuInfo.getTmId());

            // 将查询出来的基础信息封装到商品goods中
            if (baseTrademark != null){
                goods.setTmId(skuInfo.getTmId());
                goods.setTmName(baseTrademark.getTmName());
                goods.setTmLogoUrl(baseTrademark.getLogoUrl());

            }

            // 查询分类数据
            BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            // 查询分类
            if (baseCategoryView != null) {
                goods.setCategory1Id(baseCategoryView.getCategory1Id());
                goods.setCategory1Name(baseCategoryView.getCategory1Name());
                goods.setCategory2Id(baseCategoryView.getCategory2Id());
                goods.setCategory2Name(baseCategoryView.getCategory2Name());
                goods.setCategory3Id(baseCategoryView.getCategory3Id());
                goods.setCategory3Name(baseCategoryView.getCategory3Name());
            }

            // 查询平台属性
            List<SearchAttr> searchAttrs = productFeignClient.getAttrList(skuId);

            // 封装平台属性
            goods.setAttrs(searchAttrs);

            goodsRepository.save(goods);
        }
    }

    //商品下架功能
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }
}