package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.util.List;

public interface SupService {


    List<BaseSaleAttr> getBaseSaleAttrList();

    void sasaveSpuInfo(SpuInfo spuInfo);
}
