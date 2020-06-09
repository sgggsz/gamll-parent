package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ManageServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-01
 * @Description:
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 查询所有的一级分类信息
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {

        return baseCategory1Mapper.selectList(null);
    }
    /**
     * 查询所有的一级分类id,查询二级分类信息
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(wrapper);
        return baseCategory2s;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> wrapper = new QueryWrapper<>();
        wrapper.eq("category2_id",category2Id);
        List<BaseCategory3> baseCategory3s = baseCategory3Mapper.selectList(wrapper);
        return baseCategory3s;
    }


    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
// 调用mapper：
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    //添加修改
    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断添加还是修改
        if (baseAttrInfo.getId() != null) {
            baseAttrInfoMapper.updateById(baseAttrInfo);
            QueryWrapper queryWrapper = new QueryWrapper<BaseAttrValue>();
            queryWrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(queryWrapper);

        } else {
            // 新增
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        // 获取页面传递过来的所有平台属性值数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0) {
// 循环遍历
            for (BaseAttrValue baseAttrValue : attrValueList) {
// 获取平台属性Id 给attrId
                baseAttrValue.setAttrId(baseAttrInfo.getId()); // ?
                baseAttrValueMapper.insert(baseAttrValue);
            }

        }
    }

    //修改数据回显
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
    // 查询到最新的平台属性值集合数据放入平台属性中！
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }



    /**
     * 根据属性id获取属性值
     * @param attrId
     * @return
     */
    private List<BaseAttrValue> getAttrValueList(Long attrId) {
    // select * from baseAttrValue where attrId = ?
        QueryWrapper queryWrapper = new QueryWrapper<BaseAttrValue>();
        queryWrapper.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(queryWrapper);
        return baseAttrValueList;
    }

    //根据三级分类id查询，sup分页数据
    @Override
    public IPage<SpuInfo> selectPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",spuInfo.getCategory3Id());
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(pageParam, wrapper);

        return spuInfoIPage;

    }

    //分页查询sku
    @Override
    public IPage<SkuInfo> selectPageSku(Page<SkuInfo> pageParam) {
        QueryWrapper<SkuInfo> wrapper = new QueryWrapper<>();
        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(pageParam, wrapper);
        return skuInfoIPage;
    }

    //根据supid
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id",spuId);
        List<SpuImage> spuImages = spuImageMapper.selectList(wrapper);
        return spuImages;
    }

    //远程调用
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        String skuRedisKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;

        SkuInfo skuInfo = null;
        //查询缓存
        String skuInfoStr = (String)redisTemplate.opsForValue().get(skuRedisKey);

        if (StringUtils.isNotBlank(skuInfoStr)){
            skuInfo = JSON.parseObject(skuInfoStr,SkuInfo.class);
        }
        if (skuInfo == null) {
            // 用来删除分布式锁的uuid
            String uuid = UUID.randomUUID().toString();
            // 分布式锁的key，sku:15:lock
            Boolean OK = redisTemplate.opsForValue().setIfAbsent(
                    RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX
                    , uuid
                    , 10
                    , TimeUnit.SECONDS);
            if (OK){
                //查询DB
                skuInfo = getSkuInfoDB(skuId);
                if (skuInfo==null){
                    SkuInfo skuInfo1 = new SkuInfo();
                    redisTemplate.opsForValue().set(skuRedisKey,
                            JSON.toJSONString(skuInfo1),60*60,
                            TimeUnit.SECONDS);//将空的sku对象存入缓存
                    return skuInfo1;
                }
                redisTemplate.opsForValue().set(skuRedisKey, JSON.toJSONString(skuInfo));//缓存中的商品详情key

                // 使用lua脚本删除分布式锁 // lua，在get到key后，根据key的具体值删除key
                DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
                //luaScript.setResultType(Long.class);
                luaScript.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
                redisTemplate.execute(luaScript, Arrays.asList("sku:" + skuId + ":lock"), uuid);
                return skuInfo;
            }else {
                // 没有获取到分布式锁，1秒后开始自旋
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfo(skuId);
            }
        }
        return skuInfo;
    }

    /*通多DB查询sku*/
    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        // 根据skuId 查询图片列表集合
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);

        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }


    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    //查询价格
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        BigDecimal price = skuInfo.getPrice();
        return price;
    }

    //查询销售属性集合
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrs;
    }


}

