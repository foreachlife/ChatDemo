package com.example.chatdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yy
 * @data 2022/12/9 23:19
 */

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index() {

        return "index";
    }

}
