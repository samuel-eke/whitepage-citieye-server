package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegDTO;
import com.goldbyte.citieye_server.userauth.internal.entity.CitizenRegister;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegisterRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CitizenRegistrationService {
    @Autowired
    private CitizenRegisterRepository citizenRegisterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public CitizenRegister saveNewCitizen(CitizenRegDTO newCitizenRegDTO){
        CitizenRegister citizenRegister = new CitizenRegister();

        citizenRegister.setFirstName(newCitizenRegDTO.getFirstName());
        citizenRegister.setMiddleName(newCitizenRegDTO.getMiddleName());
        citizenRegister.setLastName(newCitizenRegDTO.getLastName());
        citizenRegister.setEmail(newCitizenRegDTO.getEmail());
        citizenRegister.setPhoneNumber(newCitizenRegDTO.getPhoneNumber());
        citizenRegister.setNin(passwordEncoder.encode(newCitizenRegDTO.getNin()));
        citizenRegister.setPasswordHash(passwordEncoder.encode(newCitizenRegDTO.getPassword()));
        return citizenRegisterRepository.save(citizenRegister);
    }

}
