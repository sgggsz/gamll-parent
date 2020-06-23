package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * PassportController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-22
 * @Description:
 */
@Controller
public class PassportController {

    @RequestMapping("login.html")
    public String login(String originUrl, ModelMap modelMap){
        modelMap.put("originUrl",originUrl);
        return "login";
    }
}