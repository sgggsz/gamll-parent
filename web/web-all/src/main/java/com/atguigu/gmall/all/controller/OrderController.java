package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
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

    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request , ModelMap modelMap){
        String userId = request.getHeader("userId");
        //获取用户信息
        List<UserAddress> userAddresses = userFeignClient.findUserAddressListByUserId(userId);

        //查询用户购物车选中商品
        List<CartInfo> cartInfos = cartFeignClient.getCartCheckedList(userId);

        //封装订单信息
        List<OrderDetail> orderDetails = new ArrayList<>();
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

        modelMap.put("userAddressList",userAddresses);
        modelMap.put("detailArrayList",orderDetails);

        return "order/trade";
    }
}