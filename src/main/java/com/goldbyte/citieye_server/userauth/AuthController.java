package com.goldbyte.citieye_server.userauth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/")
    public String index(){
        return "Whitepage International Concept - Citieye App";
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "this is citieye dashboard";
    }

    @GetMapping("/login")
    public String logmein(){
        return "user logged in";
    }
}
