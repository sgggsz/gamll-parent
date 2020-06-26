package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * CartApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-23
 * @Description:
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    CartInfoService cartInfoService;

    @GetMapping("getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable("userId") String userId){
        List<CartInfo> cartInfos = cartInfoService.getCartCheckedList(userId);
        return cartInfos;
    }

    @RequestMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable String skuId,@PathVariable Integer isChecked,HttpServletRequest request){
        String userIdParam = "";
        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");

        if (StringUtils.isEmpty(userId)){
            userIdParam = userTempId;
        }else {userIdParam = userId ;}

        cartInfoService.checkCart(userIdParam,skuId,isChecked);
        return Result.ok();
    }

    @RequestMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable String skuId,HttpServletRequest request){
        String userIdParam = "";
        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");

        if (StringUtils.isEmpty(userId)){
            userIdParam = userTempId;
        }else {userIdParam = userId;}
        cartInfoService.deleteCart(skuId,userIdParam);

        return Result.ok();
    }

    @RequestMapping("cartList")
    Result<List> cartList(HttpServletRequest request){
        String userIdParam = "";
        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");

        if (StringUtils.isEmpty(userId)){
            userIdParam = userTempId;
        }else {userIdParam = userId;}

       List<CartInfo> cartInfos = cartInfoService.cartList(userIdParam);
        return Result.ok(cartInfos);
    }

    @RequestMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(HttpServletRequest request, @PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum){

        String userIdParam = "";
        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");

        if (StringUtils.isEmpty(userId)){
            userIdParam = userTempId;
        }else {userIdParam = userId;}

        cartInfoService.addToCart(userIdParam,skuId,skuNum);
        return Result.ok();
    }
}