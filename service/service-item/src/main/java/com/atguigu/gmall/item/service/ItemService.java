package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * ItemService
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-07
 * @Description:
 */
public interface ItemService {
    Map<String, Object> getSkuById(Long skuId);
}