package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.Goods;
import com.atguigu.gmall.list.SearchParam;
import com.atguigu.gmall.list.SearchResponseAttrVo;
import com.atguigu.gmall.list.SearchResponseVo;
import com.atguigu.gmall.list.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ListApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-15
 * @Description:
 */
@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    SearchService searchService;

    /**
     * 创建索引
     * @return
     */
    @GetMapping("inner/createIndex")
    public Result createIndex() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @GetMapping("/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId){
        //商品上架
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId){

        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    /**
     * 商品平台属性回显
     * @param searchParam
     * @return
     */
    @RequestMapping("list")
    Result<Map> list(@RequestBody SearchParam searchParam){

        SearchResponseVo searchResponseVo = searchService.list(searchParam);

        HashMap<String, Object> map = new HashMap<>();
        map.put("goodsList",searchResponseVo.getGoodsList());
        map.put("attrsList",searchResponseVo.getAttrsList());
        map.put("trademarkList",searchResponseVo.getTrademarkList());

        return Result.ok(map);
    }

    /**
     * 为商品增加热度值
     * @param skuId
     */
    @GetMapping("inner/incrHotScore/{skuId}")
    void incrHotScore(@PathVariable("skuId")Long skuId){
        searchService.incrHotScore(skuId);
    }
}
