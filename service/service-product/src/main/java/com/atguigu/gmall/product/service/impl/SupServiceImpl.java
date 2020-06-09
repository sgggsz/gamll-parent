package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SupService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
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
    private com.atguigu.gmall.product.mapper.BaseSaleAttrMapper BaseSaleAttrMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;
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
        //保存图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            String imgName = spuImage.getImgName();
            int length = imgName.length();
            spuImage.setImgName(imgName.substring(length-19));
            spuImageMapper.insert(spuImage);
        }

    }


}