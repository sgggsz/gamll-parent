package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface BaseTrademarkService {

    IPage<BaseTrademark> selectPage(Page<BaseTrademark> baseTrademarkPage);

    BaseTrademark selectById(Long id);

    void save(BaseTrademark banner);

    void delete(Long id);

    List<BaseTrademark> getTrademarkList();

    BaseTrademark getTrademarkByTmId(Long tmId);
}
