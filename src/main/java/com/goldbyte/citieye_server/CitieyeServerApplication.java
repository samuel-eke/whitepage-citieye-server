package com.goldbyte.citieye_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CitieyeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitieyeServerApplication.class, args);
    }
    /**
     * todo: implement registration for miniors without nin
     * todo: complete registration for field officers
     * todo: add the super admin credentials
     * todo: create api where super admin can create agency admin
     * todo: supply the content to users
     * todo: implement jwt and login with UI
     * 
     * 
     * create login api for admins. include api endpoint for super admins to create
     * agency admin, get all agency admins, and get all field officers
     * publish an event and have it received by users
     */
}