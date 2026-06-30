package com.goldbyte.citieye_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitieyeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitieyeServerApplication.class, args);
    }
/**
 * todo: implement flyway migration script for the database. including all the functions and triggers written
 * todo: use docker, contanirize and deploy
 * todo: implement jwt and login with UI
 * */
}
