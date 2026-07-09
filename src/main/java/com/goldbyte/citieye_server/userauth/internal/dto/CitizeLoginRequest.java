package com.goldbyte.citieye_server.userauth.internal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for citizen login.
 * The {@code identifier} field accepts an email, NIN, phone number, or citizen code.
 */
@Getter
@Setter
public class CitizeLoginRequest {

    @NotBlank(message = "Identifier is required (email, NIN, phone number, or citizen code)")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
