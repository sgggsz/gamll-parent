package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.seckill.util.CacheHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * SeckillGoodsServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-07-02
 * @Description:
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedisTemplate redisTemplate;

    //添加秒杀商品 mysql-->redis
    @Override
    public List<SeckillGoods> selectList() {
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(null);
        return seckillGoods;
    }

    @Override
    public List<SeckillGoods> findAll() {
        return (List<SeckillGoods>)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        return (SeckillGoods)redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS,skuId+"");
    }

    //生成预备订单并一系列判断
    @Override
    public void seckillOrder(UserRecode userRecode) {

        // 查看库存
        // 产品标识， 1：可以秒杀 0：秒杀结束
        // redisTemplate.opsForValue()
        String state = (String) CacheHelper.get(userRecode.getSkuId().toString());
        if(state.equals("1")){
            // 使用setnx,setIfAbsent
            Boolean isUserSeckill = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userRecode.getUserId(), userRecode.getSkuId());
            if(!isUserSeckill){
                return;
            }
            // rightPop，抢库存
            String skuId = redisTemplate.boundListOps(RedisConst.SECKILL_GOODS +":"+ userRecode.getSkuId() + "").rightPop().toString();
            if(!StringUtils.isEmpty(skuId)){
                // 成功抢到库存
                // 生成预备订单，缓存
                OrderRecode orderRecode = new OrderRecode();
                orderRecode.setUserId(userRecode.getUserId());
                orderRecode.setNum(1);
                orderRecode.setOrderStr(MD5.encrypt(userRecode.getUserId()+userRecode.getSkuId()));
                SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(userRecode.getSkuId()+"");
                orderRecode.setSeckillGoods(seckillGoods);

                //订单数据存入Reids
                redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(), orderRecode);

                //更新库存
                updateStockCount(orderRecode.getSeckillGoods().getSkuId());

            }else{
                // 商品没货
                // 发布商品下架的通知publish
                redisTemplate.convertAndSend("seckillpush",userRecode.getSkuId()+":0");
            }
        }
    }

    //验证订单
    @Override
    public Result checkOrder(Long skuId, String userId) {

        // 下单用户是否存在
        Boolean isUserSeckill = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);// 查看是否生成了用户抢单的分布式锁key
        if(isUserSeckill){
            // 检查预备订单是否存在
            Boolean isOrderRecode = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if(isOrderRecode){
                // 已经生成预备订单，枪单成功
                OrderRecode orderRecode = (OrderRecode)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }

        // 检查是否已经正式下单
        Boolean isSeckillOrdersUsers = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        if(isSeckillOrdersUsers){
            String orderId = (String)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
            return Result.build(orderId,ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }


        // 查看库存，如果此时用户正在抢购，但是库存已空，则返回抢购失败
        String cacheStock = (String)CacheHelper.get(skuId + "");
        if(cacheStock.equals("0")){
            return Result.build(null,ResultCodeEnum.SECKILL_FINISH);
        }

        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }

    //更新库存方法
    private void updateStockCount(Long skuId) {

        // 更新的是:seckill:goods:30
        Long size = redisTemplate.boundListOps(RedisConst.SECKILL_GOODS + ":" + skuId + "").size();//当前商品的库存数量

        // seckill:goods:30
        SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId+"");
        seckillGoods.setStockCount(new BigDecimal(size).intValue());
        redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).put(skuId+"",seckillGoods);

        // 更新mysql
        seckillGoodsMapper.updateById(seckillGoods);

    }
}