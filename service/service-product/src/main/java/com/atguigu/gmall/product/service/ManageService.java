package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ManageService {
    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getAttrInfo(Long attrId);

    IPage<SpuInfo> selectPage(Page<SpuInfo> pageParam, SpuInfo spuInfo);

    IPage<SkuInfo> selectPageSku(Page<SkuInfo> pageParam);

    List<SpuImage> getSpuImageList(Long spuId);
    /**
     * 根据skuId 查询skuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 通过三级分类id查询分类信息
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);


    BigDecimal getSkuPrice(Long skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    List<JSONObject> getBaseCategoryList();

}
