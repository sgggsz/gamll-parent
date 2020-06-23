package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * UserServiceImpl
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-22
 * @Description:
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {
        String loginName = userInfo.getLoginName();
        String passwd = userInfo.getPasswd();

        passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name",loginName);
        userInfoQueryWrapper.eq("passwd",passwd);
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfoQueryWrapper);
        return userInfo1;
    }
}