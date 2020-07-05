package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.seckill.util.CacheHelper;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SeckillApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-07-01
 * @Description:
 */
@RestController
@RequestMapping("api/activity/seckill")
public class SeckillApiController {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @PostMapping("auth/submitOrder")
    public Result trade(HttpServletRequest request,@RequestBody OrderInfo orderInfo)  {
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        // 插入订单数据库
        // 返回订单id
        Long orderId = orderFeignClient.submitOrder(orderInfo);


        // 向缓存，已经正式提交订单的集合中加入用户信息RedisConst.SECKILL_ORDERS_USERS = "seckill:orders:users";//已经正式下单用户列表
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId,orderId+"");
        // 删除预订单RedisConst.SECKILL_ORDERS = "seckill:orders";//已经创建的预备订单列表
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);

        return Result.ok(orderId);
    }

    @RequestMapping("auth/trade")
    Result<Map<String,Object>> trade(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        // 先得到用户想要购买的商品！
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);

        // 收获地址列表
        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(orderRecode.getUserId());

        // 商品详情列表
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(orderRecode.getSeckillGoods().getSkuId());

        // 声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getPrice());
        // 添加到集合
        detailArrayList.add(orderDetail);

        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        Map<String, Object> result = new HashMap<>();
        result.put("userAddressList", userAddressListByUserId);
        result.put("detailArrayList", detailArrayList);
        // 保存总金额
        result.put("totalAmount", orderInfo.getTotalAmount());
        return Result.ok(result);

    }

    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) throws Exception {
        String userId = AuthContextHolder.getUserId(request);
        Result result =  seckillGoodsService.checkOrder(skuId, userId);
        return result;
    }

    /**
     * 根据用户和商品ID实现秒杀下单
     *
     * @param skuId
     * @return
     */
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) throws Exception {
        //校验下单码（抢购码规则可以自定义）
        String userId = AuthContextHolder.getUserId(request);
        String skuIdStr = request.getParameter("skuIdStr");
        if (!skuIdStr.equals(MD5.encrypt(userId))) {
            //请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }

        //产品标识， 1：可以秒杀 0：秒杀结束
        String state = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(state)) {
            //请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        if ("1".equals(state)) {
            //用户记录
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);
            // 发消息，预备下单到缓存
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, userRecode);
        } else {
            //已售罄
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }
        return Result.ok();
    }


    @RequestMapping("auth/getSeckillSkuIdStr/{skuId}")
    Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request){
        String userId = request.getHeader("userId");
        String publish = (String) CacheHelper.get(skuId+"");

        if(null!=publish&&publish.equals("1")){
            //可以动态生成，放在redis缓存
            String skuIdStr = MD5.encrypt(userId);
            return Result.ok(skuIdStr);
        }else {
            return Result.fail().message("获取下单码失败");
        }
    }

    @RequestMapping("findAll")
    Result findAll(){
        List<SeckillGoods> seckillGoods = seckillGoodsService.findAll();

        return Result.ok(seckillGoods);
    }

    @RequestMapping("getSeckillGoods/{skuId}")
    Result getSeckillGoods(@PathVariable("skuId") Long skuId){

        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);

        return Result.ok(seckillGoods);
    }

    //http://localhost:8200/api/activity/seckill/1
    @RequestMapping("1")
    void a(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_1,"11");
    }
}