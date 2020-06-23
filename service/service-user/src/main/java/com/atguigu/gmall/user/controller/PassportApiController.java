package com.atguigu.gmall.user.controller;

/**
 * PassportApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-22
 * @Description:
 */

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户认证接口
 * </p>
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     *
     * @param userInfo
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo) {
        UserInfo info = userService.login(userInfo);

        if (info != null) {
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", info.getName());
            map.put("nickName", info.getNickName());
            map.put("token", token);
            // token写入缓存
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,info.getId(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            //userService.putUserToken(token,userInfo.getId()+"");

            // token写入cookie
            return Result.ok(map);
        } else {
            return Result.fail().message("用户名或密码错误");
        }
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request) {
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + request.getHeader("token"));
        return Result.ok();
    }
}