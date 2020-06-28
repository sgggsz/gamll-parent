package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-24
 * @Description:
 */
@Controller
public class OrderController {

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, ModelMap modelMap) {
        String userId = request.getHeader("userId");
        // 查询用户信息
        // userAddressList
        List<UserAddress> userAddresses = userFeignClient.findUserAddressListByUserId(userId);
        modelMap.put("userAddressList",userAddresses);

        // 查询订单详情(购物车)
        // detailArrayList//被选中的购物车集合
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<CartInfo> cartInfos = cartFeignClient.getCartCheckedList(userId);

        orderDetails = cartInfos.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            // 将购物车数据封装给订单详情
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());
        modelMap.put("detailArrayList",orderDetails);

        // 页面商品总数量和结算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetails);
        orderInfo.sumTotalAmount();// 核算价格
        modelMap.put("totalNum",cartInfos.size());
        modelMap.put("totalAmount",orderInfo.getTotalAmount());
        String tradeNo = orderFeignClient.getTradeNo(userId);
        modelMap.put("tradeNo",tradeNo);


        return "order/trade";
    }
}