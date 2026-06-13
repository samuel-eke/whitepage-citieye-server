package com.goldbyte.citieye_server.userauth.internal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenRegDTO {

    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    @JsonAlias("firstName")
    private String firstName;

    @JsonProperty("middle_name")
    @JsonAlias("middleName")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    @JsonAlias("lastName")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^0\\d{10}$", message = "Phone number must be 11 digits and start with 0")
    @JsonProperty("phone_number")
    @JsonAlias("phonenumber")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Password must contain only alphanumeric characters")
    private String password;

    @NotBlank(message = "NIN is required")
    @Size(min = 10, max = 10, message = "NIN must be exactly 10 characters")
    private String nin;
}
