package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * CartController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-23
 * @Description:
 */
@Controller
public class CartController {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    UserFeignClient userFeignClient;




    @RequestMapping("cart.html")
    public String Cart(ModelMap modelMap){
        Result<List> result = cartFeignClient.cartList();
        List data = result.getData();
        modelMap.put("data",data);
        return "cart/index";
    }

    @RequestMapping("addCart.html")
    public String addToCart(CartInfo cartInfo, HttpServletRequest request, ModelMap modelMap){
        //获取添加购物车的商品
        SkuInfo skuInfo = productFeignClient.getSkuInfo(cartInfo.getSkuId());
        modelMap.put("skuInfo",skuInfo);

        //调用购物车服务的fegin
        Result result = cartFeignClient.addToCart(cartInfo.getSkuId(), cartInfo.getSkuNum());

        return "redirect:http://cart.gmall.com/cart/addCart.html";
    }
}