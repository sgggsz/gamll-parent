package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AuthGlobalFilter
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-21
 * @Description:
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {


    @Autowired
    RedisTemplate redisTemplate;


    // 匹配路径的工具类
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private String authUrls;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取到请求对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 不拦截passport请求
        String  passportUrl = request.getURI().toString();
        if(passportUrl.indexOf("passport")!=-1||passportUrl.indexOf(".css")!=-1||passportUrl.indexOf(".ico")!=-1||passportUrl.indexOf(".js")!=-1){
            // 放行登录方法
            return chain.filter(exchange);
        }
        // 获取Url（当前请求）
        String path = request.getURI().getPath();
        String uri = request.getURI().toString();
        // 如果是内部接口，则网关拦截不允许外部访问！
        if (antPathMatcher.match("/**/inner/**", path)) {

            return out(response, ResultCodeEnum.PERMISSION);
        }

        String userId = getUserId(request);//获得userid的方法
        String[] split = authUrls.split(",");

        // 页面ajax异步访问的api接口
        if(antPathMatcher.match("/api/**/auth/**",path)){
            //禁止访问内部接口，直接返回数据，包括错误代码json字符串
            if (StringUtils.isEmpty(userId)){
                return out(response,ResultCodeEnum.PERMISSION);
            }
        }
        //判断白名单的拦截
        for (String webUrl : split) {
            //在白名单中的访问url都必须验证过用户身份
            // 在白名单中的访问url都必须验证过用户身份，此处过的userId
            if(path.indexOf(webUrl)!=-1&& StringUtils.isEmpty(userId)){
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+request.getURI());
                return response.setComplete();
            }

        }

        //如果用户id是正常的可以访问的，可以将y用户id放入request中，方便后面获取，一般可以放在header中
        if (!StringUtils.isEmpty(userId)){
            request.mutate().header("userId",userId).build();
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    private String getUserId(ServerHttpRequest request) {
        String userId = "";
        //已经被网关验证过身份的用户id，可以直接从header中获取
        List<String> strings = request.getHeaders().get("userId");
        if (null != strings){
            userId = strings.get(0);
        }

        //第一次被网关验证身份的，通过token获取userID
        if (StringUtils.isEmpty(userId)){
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie cookie = cookies.getFirst("token");
            if (cookie!=null){
                String token = URLDecoder.decode(cookie.getValue());
                String key = RedisConst.USER_LOGIN_KEY_PREFIX + token;
                Object o = redisTemplate.opsForValue().get(key);
                if(null!=o){
                    userId = JSONObject.toJSONString(o);
                }
            }

        }
        return userId;
    }

    // 接口鉴权失败返回数据
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
// 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
// 输入到页面
        return response.writeWith(Mono.just(wrap));
    }

}

