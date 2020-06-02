package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttrValue;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SupService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SupServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-02
 * @Description:
 */
@Service
public class SupServiceImpl implements SupService {

    @Autowired
    private BaseSaleAttrMapper BaseSaleAttrMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    //差选属性名称
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return BaseSaleAttrMapper.selectList(new QueryWrapper<BaseSaleAttr>());
    }

    //保存方法
    @Override
    @Transactional
    public void sasaveSpuInfo(SpuInfo spuInfo) {
        //保存Spu
        spuInfoMapper.insert(spuInfo);
        //保存SpuSaleAttr
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList.size() > 0){
            for (int i = 0; i <spuSaleAttrList.size(); i++) {
                SpuSaleAttr spuSaleAttr = spuSaleAttrList.get(i);
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                //保存SpuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();


                if (spuSaleAttrValueList.size()>0){
                    for (int j = 0; j <spuSaleAttrValueList.size() ; j++) {
                        SpuSaleAttrValue spuSaleAttrValue = spuSaleAttrValueList.get(j);
                        spuSaleAttrValue.setSpuId(spuSaleAttr.getSpuId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }


    }


}