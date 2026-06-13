package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegDTO;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegisterRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginReg {

    @Autowired
    private CitizenRegisterRepository citizenRegisterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/user/register")
    public ResponseEntity<String> newCitizen(@Valid @RequestBody CitizenRegDTO citizenRegDTO) {

        return null;
    }

}
