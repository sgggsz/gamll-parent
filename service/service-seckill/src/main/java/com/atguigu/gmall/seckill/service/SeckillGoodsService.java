package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;

import java.util.List;

public interface SeckillGoodsService {
    List<SeckillGoods> selectList();

    List<SeckillGoods> findAll();

    SeckillGoods getSeckillGoods(Long skuId);

    void seckillOrder(UserRecode userRecode);

    Result checkOrder(Long skuId, String userId);
}
