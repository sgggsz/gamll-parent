package com.atguigu.gmall.product.controller;

import com.atguigu.gamll.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SupService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SupController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-02
 * @Description:
 */
@Api(tags = "sup接口")
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SupController {

    @Autowired
    private ManageService manageService;

    @Autowired
    private SupService supService;

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /**
     * 根据三级分类id获取spu列表分类
     */
    @GetMapping("{page}/{size}")
    public Result<IPage<SpuInfo>> index(@PathVariable long page,
                                        @PathVariable long size,
                                        SpuInfo spuInfo){
        Page<SpuInfo> pageParam = new Page<>(page, size);
        IPage<SpuInfo> spuInfoIPage =  manageService.selectPage(pageParam,spuInfo);
        return Result.ok(spuInfoIPage);
    }

    //查询品牌
    @GetMapping("baseTrademark/getTrademarkList")
    public Result<List<BaseTrademark>> getTrademarkList() {
        List<BaseTrademark> baseTrademarkList = baseTrademarkService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }

    //查询销售属性名称
    @GetMapping("baseSaleAttrList")
    public Result<List<BaseSaleAttr>> getBaseSaleAttrList() {
        List<BaseSaleAttr> list = supService.getBaseSaleAttrList();
        return Result.ok(list);
    }

    //添加sup
    @PostMapping("saveSpuInfo")
    public Result sasaveSpuInfo(@RequestBody SpuInfo spuInfo){
        supService.sasaveSpuInfo(spuInfo);
        return Result.ok();
    }
}