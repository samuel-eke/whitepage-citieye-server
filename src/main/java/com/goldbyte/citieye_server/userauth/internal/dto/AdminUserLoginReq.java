package com.goldbyte.citieye_server.userauth.internal.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserLoginReq {

    @NotBlank(message = "Provide your CitiEye code")
    private String code;

    @NotBlank(message = "Password is required")
    private String password;
}
