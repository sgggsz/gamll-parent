package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartInfoService {
    void addToCart( String userIdParam, Long skuId, Integer skuNum);

    List<CartInfo> cartList(String userIdParam);

    void deleteCart(String skuId,String userIdParam);

    void checkCart(String userIdParam, String skuId, Integer isChecked);

    List<CartInfo> getCartCheckedList(String userId);

    void mergeCart(String userId, String userTempId);
}
