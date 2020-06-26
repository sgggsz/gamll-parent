package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UserApiController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-24
 * @Description:
 */
@RestController
@RequestMapping("/api/user/")
public class UserApiController {

    @Autowired
    UserService userService;

    @RequestMapping("inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") String userId){
        List<UserAddress> userAddresses = userService.findUserAddressListByUserId(userId);
        return userAddresses;
    }
}