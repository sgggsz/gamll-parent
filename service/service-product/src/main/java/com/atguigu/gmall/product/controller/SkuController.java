package com.atguigu.gmall.product.controller;

import com.atguigu.gamll.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * SkuController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-02
 * @Description:
 */
@Api(tags = "商品基础属性接口")
@RestController
@RequestMapping("admin/product")
@CrossOrigin
public class SkuController {
    @Autowired
    private ManageService manageService;

    //分页查询sku
    @GetMapping("/list/{page}/{size}")
    public Result<IPage<SkuInfo>> index(@PathVariable long page,
                                        @PathVariable long size){
        Page<SkuInfo> pageParam = new Page<>(page, size);
        IPage<SkuInfo> spuInfoIPage =  manageService.selectPageSku(pageParam);
        return Result.ok(spuInfoIPage);
    }

}