package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders ="**" )
public class HelloWorldController {

    @RequestMapping("/hello")
    public TestCmd  helloWorld(){
        System.out.println("hello wolrd api invoked");
        TestCmd cmd = new TestCmd();
        cmd.setMessage("hello world");

        return cmd;
    }
}

class TestCmd{
    private String message;

    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

}