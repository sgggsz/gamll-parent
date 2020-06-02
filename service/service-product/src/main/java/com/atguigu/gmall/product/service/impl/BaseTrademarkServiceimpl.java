package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BaseTrademarkServiceimpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-02
 * @Description:
 */
@Service
public class BaseTrademarkServiceimpl implements BaseTrademarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    //分页查询商标
    @Override
    public IPage<BaseTrademark> selectPage(Page<BaseTrademark> baseTrademarkPage) {
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkMapper.selectPage(baseTrademarkPage, baseTrademarkQueryWrapper);
        return baseTrademarkIPage;
    }

    //根据id查询
    @Override
    public BaseTrademark selectById(Long id) {
        BaseTrademark baseTrademark = new BaseTrademark();
        baseTrademark.setId(id);
        BaseTrademark baseTrademark1 = baseTrademarkMapper.selectById(baseTrademark);
        return baseTrademark1;
    }

    //添加or修改
    @Override
    public void save(BaseTrademark banner) {
        if (banner.getId() != null){
            //修改
            baseTrademarkMapper.updateById(banner);
        }else {
            baseTrademarkMapper.insert(banner);
        }
    }

    //删除
    @Override
    public void delete(Long id) {
        baseTrademarkMapper.deleteById(id);
    }

    //查询所有给sup用
    @Override
    public List<BaseTrademark> getTrademarkList() {
        QueryWrapper<BaseTrademark> wrapper = new QueryWrapper<>();
        List<BaseTrademark> baseTrademarks = baseTrademarkMapper.selectList(wrapper);
        return baseTrademarks;
    }
}