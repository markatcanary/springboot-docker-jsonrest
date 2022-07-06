package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders ="**" )
public class HealthController {

    @RequestMapping("/")
    public TestCmd  health(){
        System.out.println("health check logging");
        TestCmd cmd = new TestCmd();
        cmd.setMessage("health check passed");
        return cmd;
    }
}
