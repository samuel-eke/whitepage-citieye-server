package com.goldbyte.citieye_server.userauth.internal.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AdminUserLoginResp {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    /** JWT token issued on successful login */
    private String token;
    private String citizenCode;
    /** The admin's role: agency_admin or super_admin */
    private String role;
    private String stateOfResidence;
    private String lga;
    private String residenceLga;
    /** Granular permissions granted to this admin via RBAC */
    private List<String> permissions;
    private String message;
}
