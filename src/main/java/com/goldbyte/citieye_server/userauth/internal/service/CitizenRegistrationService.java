package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.internal.dto.CitizenRegDTO;
import com.goldbyte.citieye_server.userauth.internal.entity.CitizenRegister;
import com.goldbyte.citieye_server.userauth.internal.entity.UserRole;
import com.goldbyte.citieye_server.userauth.internal.repo.CitizenRegisterRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CitizenRegistrationService {
    @Autowired
    private CitizenRegisterRepository citizenRegisterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public CitizenRegister saveNewCitizen(CitizenRegDTO newCitizenRegDTO){
        CitizenRegister citizenRegister = new CitizenRegister();

        citizenRegister.setId(UUID.randomUUID());
        citizenRegister.setFirstName(newCitizenRegDTO.getFirstName());
        citizenRegister.setMiddleName(newCitizenRegDTO.getMiddleName());
        citizenRegister.setLastName(newCitizenRegDTO.getLastName());
        citizenRegister.setEmail(newCitizenRegDTO.getEmail());
        citizenRegister.setPhoneNumber(newCitizenRegDTO.getPhoneNumber());
        citizenRegister.setNin(newCitizenRegDTO.getNin()); // Keep NIN plain or hash based on preference (NIN must be exactly 10 characters bpchar)
        citizenRegister.setPasswordHash(passwordEncoder.encode(newCitizenRegDTO.getPassword()));
        citizenRegister.setRole(UserRole.citizen);
        citizenRegister.setIsActive(true);
        citizenRegister.setIsVerified(false);
        return citizenRegisterRepository.save(citizenRegister);
    }

}

