package com.atguigu.gmall.list.service;

import com.atguigu.gmall.list.SearchParam;
import com.atguigu.gmall.list.SearchResponseVo;

public interface SearchService {
    void upperGoods(Long skuId);

    void lowerGoods(Long skuId);

    SearchResponseVo list(SearchParam searchParam);

    void incrHotScore(Long skuId);
}
