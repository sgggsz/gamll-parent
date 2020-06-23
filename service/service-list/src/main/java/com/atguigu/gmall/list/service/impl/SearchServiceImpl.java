package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.*;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import jodd.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.elasticsearch.common.text.Text;

import java.io.IOException;
import java.util.*;

/**
 * SearchServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-16
 * @Description:
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    RedisTemplate redisTemplate;

    //商品上架功能
    @Override
    public void upperGoods(Long skuId) {

        Goods goods = new Goods();
        //查询goods信息，skuInfo,trademark,category,baseAttrInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo!=null) {
            // 封装商品数据
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setCreateTime(new Date());

            // 查询商标数据
            BaseTrademark baseTrademark = productFeignClient.getTrademarkByTmId(skuInfo.getTmId());

            // 将查询出来的基础信息封装到商品goods中
            if (baseTrademark != null){
                goods.setTmId(skuInfo.getTmId());
                goods.setTmName(baseTrademark.getTmName());
                goods.setTmLogoUrl(baseTrademark.getLogoUrl());

            }

            // 查询分类数据
            BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            // 查询分类
            if (baseCategoryView != null) {
                goods.setCategory1Id(baseCategoryView.getCategory1Id());
                goods.setCategory1Name(baseCategoryView.getCategory1Name());
                goods.setCategory2Id(baseCategoryView.getCategory2Id());
                goods.setCategory2Name(baseCategoryView.getCategory2Name());
                goods.setCategory3Id(baseCategoryView.getCategory3Id());
                goods.setCategory3Name(baseCategoryView.getCategory3Name());
            }

            // 查询平台属性
            List<SearchAttr> searchAttrs = productFeignClient.getAttrList(skuId);

            // 封装平台属性
            goods.setAttrs(searchAttrs);

            goodsRepository.save(goods);
        }
    }

    //商品下架功能
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {

        //调用检索接口
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);//吊桶DSL语句方法
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //解析返回结果
        SearchResponseVo searchResponseVo = this.parseSearchResult(searchResponse);
        return searchResponseVo;

    }



    //构建返回结果
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {

        //创建返回对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();



        SearchHits allHits = searchResponse.getHits();
        SearchHit[] sourcehits = allHits.getHits();

        ArrayList<Goods> goods = new ArrayList<>();

        //构建商标去重集合
        HashSet<SearchResponseTmVo> searchResponseTmVosSet = new HashSet<>();

        //构建属性去重集合
        HashSet<SearchAttr> searchAttrsSet = new HashSet<>();
        for (SearchHit sourceHit : sourcehits) {
            String sourceAsString = sourceHit.getSourceAsString();

            Goods good = JSON.parseObject(sourceAsString, Goods.class);
            // 解析高亮
            Map<String, HighlightField> highlightFields = sourceHit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            if(null!=title){
                Text[] fragments = title.getFragments();
                Text fragment = fragments[0];
                good.setTitle(fragment.toString());
            }
            System.out.println(good);

            //封装商标集合（hashset去重）
            Long tmId = good.getTmId();
            String tmName = good.getTmName();
            String tmLogoUrl = good.getTmLogoUrl();
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            searchResponseTmVo.setTmId(tmId);
            searchResponseTmVo.setTmName(tmName);
                    searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            searchResponseTmVosSet.add(searchResponseTmVo);

            //封装属性集合进行去重
            List<SearchAttr> attrs = good.getAttrs();
            for (SearchAttr attr : attrs) {
                searchAttrsSet.add(attr);
            }
            goods.add(good);
        }
        //封装GoodsList属性
        searchResponseVo.setGoodsList(goods);

        //封装List<SearchResponseTmVo> trademarkList属性
        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
        for (SearchResponseTmVo searchResponseTmVo : searchResponseTmVosSet) {
            searchResponseTmVos.add(searchResponseTmVo);
        }
        searchResponseVo.setTrademarkList(searchResponseTmVos);

        //封装List<SearchResponseAttrVo> attrsList 属性
        HashMap<Long, String> attrIdAttrName = new HashMap<>();

        for (SearchAttr searchAttr : searchAttrsSet) {

            attrIdAttrName.put(searchAttr.getAttrId(),searchAttr.getAttrName());
        }

        List<SearchResponseAttrVo> attrsList = new ArrayList<>();
        Set<Map.Entry<Long, String>> entries = attrIdAttrName.entrySet();
        for (Map.Entry<Long, String> entry : entries) {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            searchResponseAttrVo.setAttrId(entry.getKey());
            List<String> attrValueList = searchResponseAttrVo.getAttrValueList();
            //遍历所有属性对象
            for (SearchAttr searchAttr : searchAttrsSet) {
                if (entry.getKey() == searchAttr.getAttrId()){
                    attrValueList.add(searchAttr.getAttrValue());
                }
            }
            String attrName = entry.getValue();
            searchResponseAttrVo.setAttrName(attrName);
            attrsList.add(searchResponseAttrVo);
        }
        searchResponseVo.setAttrsList(attrsList);

        return searchResponseVo;
    }

    //封装dsl语句（es的sql）
    private SearchRequest buildQueryDsl(SearchParam searchParam) {

        //分页参数
        Integer pageSize = searchParam.getPageSize();
        Integer pageNo = searchParam.getPageNo();

        //品牌
        String trademark = searchParam.getTrademark();

        //选中的平台属性集合
        String[] props = searchParam.getProps();

        //排序参数
        String order = searchParam.getOrder();

        //关键字
        String keyword = searchParam.getKeyword();

        //三级分类id
        Long category1Id = searchParam.getCategory1Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category3Id = searchParam.getCategory3Id();



        //构建dsl语句
        //总的{}语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool复合查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //根据检索条件封装复合查询
        if (StringUtil.isNotBlank(keyword)){
            boolQueryBuilder.must(new MatchQueryBuilder("title",keyword));

            // 可以使用默认高亮样式，也可以自定义高亮样式（高亮查询）
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.postTags("</span>");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder;'>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        if (StringUtil.isNotBlank(trademark)){
            String[] split = trademark.split(":");
            boolQueryBuilder.filter(new TermQueryBuilder("tmId",split[0]));
        }

        if (null != category1Id) {
            boolQueryBuilder.filter(new TermQueryBuilder("category1Id", category1Id));
        }
        if (null != category2Id) {
            boolQueryBuilder.filter(new TermQueryBuilder("category2Id", category2Id));
        }
        if (null != category3Id) {
            boolQueryBuilder.filter(new TermQueryBuilder("category3Id", category3Id));
        }

        if (null != props && props.length > 0) {
            for (String prop : props) {
                // prop = 23:4G:运行内存
                String[] split = prop.split(":");
                String attrId = split[0];//属性id
                String attrValue = split[1];//属性值名称
                String attrName = split[2];

                // 最内层bool查询，匹配查询
                BoolQueryBuilder subBoolQueryForProps = new BoolQueryBuilder();
                subBoolQueryForProps.must(new MatchQueryBuilder("attrs.attrValue", attrValue));
                subBoolQueryForProps.must(new MatchQueryBuilder("attrs.attrId", attrId));

                // 第二层嵌套匹配条件bool查询
                BoolQueryBuilder boolQueryForProps = new BoolQueryBuilder();
                boolQueryForProps.must(new NestedQueryBuilder("attrs", subBoolQueryForProps, ScoreMode.None));

                // 封装进外层的bool，过滤
                boolQueryBuilder.filter(boolQueryForProps);
            }
        }

        //封装排序语句
        if (StringUtils.isNotBlank(order)){
            String[] split = order.split(":");
            String type = split[0];
            String sort = split[1];

            if (type.equals("1")){
                type = "hotScore";
            }else {
                type = "price";
            }

            searchSourceBuilder.sort(type,sort.equals("asc")? SortOrder.ASC:SortOrder.DESC);

        }

//        //商标聚合
//        TermsAggregationBuilder termsAggregationBuilderMark = AggregationBuilders.terms("tmIdAgg").field("tmId")
//                                        .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
//                                        .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
//
//        searchSourceBuilder.aggregation(termsAggregationBuilderMark);
//        //属性聚合
//        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
//                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
//                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
//                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));


        //将复合搜索条件放入query
        searchSourceBuilder.query(boolQueryBuilder);

        //构建请求
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);

        // 打印dsl语句
        System.out.println(searchSourceBuilder.toString());

        return searchRequest;
    }

    //每次访问商品详情,增加商品热度值
    @Override
    public void incrHotScore(Long skuId) {
        //先更新redis分数
        Double hotScore = redisTemplate.opsForZSet().incrementScore("hotScore", "sku:" + skuId, 1);

        if (hotScore % 10 == 0) {
            //跟新es分数
            Optional<Goods> byId = goodsRepository.findById(skuId);
            Goods goods = byId.get();
            goods.setHotScore(Math.round(hotScore));
            goodsRepository.save(goods);
        }
    }
}