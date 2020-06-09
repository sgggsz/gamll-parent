package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SkuController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-02
 * @Description:
 */
@Api(tags = "商品SKU接口")
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SkuController {
    @Autowired
    private ManageService manageService;

    @Autowired
    private SkuService skuService;

    //分页查询sku
    @GetMapping("/list/{page}/{size}")
    public Result<IPage<SkuInfo>> index(@PathVariable long page,
                                        @PathVariable long size){
        Page<SkuInfo> pageParam = new Page<>(page, size);
        IPage<SkuInfo> spuInfoIPage =  manageService.selectPageSku(pageParam);
        return Result.ok(spuInfoIPage);
    }

    //根据spuId查询所有图片
    @GetMapping("spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    /**
     * 根据spuId 查询销售属性集合
     * @param spuId
     * @return
     */
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = skuService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    /**
     * 保存sku
     * @param skuInfo
     * @return
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
// 调用服务层
        skuService.saveSkuInfo(skuInfo);
        return Result.ok();
    }
    /**
     * 商品上架
     * @param skuId
     * @return
     */
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        skuService.onSale(skuId);
        return Result.ok();
    }

    /**
     * 商品下架
     * @param skuId
     * @return
     */
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        skuService.cancelSale(skuId);
        return Result.ok();
    }


}