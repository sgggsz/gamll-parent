package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * a
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-27
 * @Description:
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OderDetailMapper oderDetailMapper;

    @Autowired
    RedisTemplate redisTemplate;

    //生成订单信息
    @Override
    public OrderInfo saveOrderInfo(OrderInfo orderInfo) {

        orderInfoMapper.insert(orderInfo);
        Long id = orderInfo.getId();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            oderDetailMapper.insert(orderDetail);
        }

        return orderInfo;
    }


    @Override
    public String getTradeNo(String userId) {
        //定义key
        String tradeNokey = "user:"+userId+":tradeNo";
        //定义流水号
        String tradeNoValue = UUID.randomUUID().toString().replace("-", "");
        //进如redis
        redisTemplate.opsForValue().set(tradeNokey,tradeNoValue,15*60, TimeUnit.SECONDS);
        return tradeNoValue;
    }

    @Override
    public boolean checkTradeNo(String userId, String tradeNo) {
        boolean b = false;

        //获取
        String tradeNokey = "user:"+userId+":tradeNo";

        String tradeNoValue = (String)redisTemplate.opsForValue().get(tradeNokey);
//        if (tradeNo.equals(tradeNoValue)){
//            b = true;
//            deleteTradeNo(userId);
//        }

        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
        luaScript.setResultType(Long.class);
        luaScript.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
        Long execute = (Long)redisTemplate.execute(luaScript, Arrays.asList(tradeNokey), tradeNo);// 1成功 0失败

        if(execute==1){
            b = true;
        }
        return b;

    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetails = oderDetailMapper.selectList(wrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

//    public void deleteTradeNo(String userId) {
//        redisTemplate.delete("user:"+ userId + ":tradeNo");
//    }
}