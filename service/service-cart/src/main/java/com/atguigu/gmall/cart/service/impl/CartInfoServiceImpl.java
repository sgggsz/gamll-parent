package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * i
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-23
 * @Description:
 */
@Service
public class CartInfoServiceImpl implements CartInfoService {


    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    //添加购物车
    @Override
    public void addToCart(String userIdParam, Long skuId, Integer skuNum) {
        String userId = userIdParam;

        //操作数据库
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sku_id",skuId);
        CartInfo cartInfo = cartInfoMapper.selectOne(wrapper);
        if (null!=cartInfo){
            //更新
            cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
            cartInfoMapper.updateById(cartInfo);
        }else {
            //添加
            //查询sku信息
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo = new CartInfo();
            cartInfo.setSkuNum(skuNum);
            cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setIsChecked(1);
            cartInfo.setSkuId(skuInfo.getId());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setUserId(userId);
            cartInfoMapper.insert(cartInfo);
        }

        //同步缓存
        loadCartCache(userId);
    }



    private List<CartInfo> loadCartCache(String userId) {

        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<CartInfo> cartInfos = cartInfoMapper.selectList(wrapper);

        if (null!=cartInfos){
            HashMap<String, CartInfo> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
                cartInfo.setSkuPrice(skuPrice);
                map.put(cartInfo.getSkuId()+"",cartInfo);
            }
            redisTemplate.opsForHash().putAll(getUserCartKey(userId),map);
        }
        return cartInfos;

    }
    public String getUserCartKey(String userId){

        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }

    //购物车列表功能
    @Override
    public List<CartInfo> cartList(String userIdParam) {
        List<CartInfo> values = redisTemplate.opsForHash().values(getUserCartKey(userIdParam));

        return values;
    }
    //删除购物车
    @Override
    public void deleteCart(String skuId,String userIdParam) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id",skuId);
        wrapper.eq("user_id",userIdParam);

        cartInfoMapper.delete(wrapper);
        //删除缓存

        redisTemplate.opsForHash().delete(getUserCartKey(userIdParam),skuId);
    }

    //跟新选中状态
    @Override
    public void checkCart(String userIdParam, String skuId, Integer isChecked) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sku_id",skuId);
        wrapper.eq("user_id",userIdParam);
        CartInfo cartInfo = cartInfoMapper.selectOne(wrapper);
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.update(cartInfo,wrapper);

        //跟新缓存
        cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
        redisTemplate.opsForHash().put(getUserCartKey(userIdParam),skuId,cartInfo);
    }

    //获取购物车中被选中的商品（生成订单用）
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        cartInfos = redisTemplate.opsForHash().values(getUserCartKey(userId));
        if (null == cartInfos || cartInfos.size()<=0 ){
            cartInfos = loadCartCache(userId);
        }
       //删除未选中的商品
        Iterator<CartInfo> iterator = cartInfos.iterator();
        while(iterator.hasNext()){
            CartInfo next = iterator.next();
            if(new BigDecimal(next.getIsChecked()+"").compareTo(new BigDecimal("0"))==0){
                iterator.remove();
            }
        }
        return cartInfos;
    }
}