package com.goldbyte.citieye_server.userauth.internal.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CitizenRegResponse {
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
    private String citizenCode;
    private String userRole;
    private String cohortPlacement;
    private Integer age;
    private String message;
}
