package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * BaseTrademarkController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-02
 * @Description:
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
@CrossOrigin
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @ApiOperation(value = "分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {

        Page<BaseTrademark> pageParam = new Page<>(page, limit);
        IPage<BaseTrademark> pageModel = baseTrademarkService.selectPage(pageParam);
        return Result.ok(pageModel);
    }

    //根据id获取数据
    @ApiOperation(value = "获取BaseTrademark")
    @GetMapping("get/{id}")
    public Result<BaseTrademark> get(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.selectById(id);
        return Result.ok(baseTrademark);
    }

    //添加
    @ApiOperation(value = "新增BaseTrademark")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark banner) {
        baseTrademarkService.save(banner);
        return Result.ok();
    }

    //修改
    @ApiOperation(value = "修改BaseTrademark")
    @PutMapping("update")
    public Result updateById(@RequestBody BaseTrademark banner) {
        baseTrademarkService.save(banner);
        return Result.ok();
    }

    @ApiOperation(value = "删除BaseTrademark")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        baseTrademarkService.delete(id);
        return Result.ok();
    }
}