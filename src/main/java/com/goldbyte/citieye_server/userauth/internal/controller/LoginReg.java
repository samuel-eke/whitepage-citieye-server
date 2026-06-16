package com.goldbyte.citieye_server.userauth.internal.controller;

import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegDTO;
import com.goldbyte.citieye_server.userauth.internal.entity.CitizenRegister;
import com.goldbyte.citieye_server.userauth.internal.service.CitizenRegistrationService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginReg {

    @Autowired
    private CitizenRegistrationService citizenRegistrationService;

    @PostMapping("/user/register")
    public ResponseEntity<CitizenRegister> newCitizen(@Valid @RequestBody CitizenRegDTO citizenRegDTO) {
        CitizenRegister registered = citizenRegistrationService.saveNewCitizen(citizenRegDTO);
        return ResponseEntity.ok(registered);
    }

}

