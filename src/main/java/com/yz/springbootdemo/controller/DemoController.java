package com.yz.springbootdemo.controller;

import com.yz.springbootdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class DemoController {
    private static final int _1MB = 1024 * 1024;

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String hello() {
        return "Hello!!!";
    }

    @GetMapping("/loop")
    public String loop(){
        return userService.findAll(true);
    }

    @GetMapping("/deadlock")
    public String deadlock(){
        return userService.findAll2();
    }

    @GetMapping("/testAllocation")
    public String testAllocation() throws InterruptedException {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[_1MB * 1];
        allocation2 = new byte[_1MB * 3];
        allocation3 = new byte[_1MB * 2];
        allocation4 = new byte[_1MB * 3];
        TimeUnit.SECONDS.sleep(1L);
        System.out.println("testAllocation");
        return "testAllocation";
    }
}
