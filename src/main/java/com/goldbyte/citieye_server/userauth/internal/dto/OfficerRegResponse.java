package com.goldbyte.citieye_server.userauth.internal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerRegResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private String nin;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String address;
    private String stateOfResidence;
    private String stateOfOrigin;
    private String lga;
    private String residenceLga;
    private String citizenCode;
    private String userRole;
    private String specialty;
    private String token; // Access JWT token for immediate login upon registration
    private String message;
}
