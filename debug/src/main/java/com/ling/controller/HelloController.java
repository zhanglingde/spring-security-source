package com.ling.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // 需要登录才可访问
    @GetMapping("/hello")
    public String hello(){
        return "hello spring security";
    }
}