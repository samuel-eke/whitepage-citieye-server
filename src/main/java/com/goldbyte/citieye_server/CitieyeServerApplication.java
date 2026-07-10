package com.goldbyte.citieye_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
     */
}