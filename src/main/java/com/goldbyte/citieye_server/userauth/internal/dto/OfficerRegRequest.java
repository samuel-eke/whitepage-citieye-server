package com.goldbyte.citieye_server.userauth.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class OfficerRegRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name cannot exceed 255 characters")
    private String firstName;

    @Size(max = 255, message = "Middle name cannot exceed 255 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name cannot exceed 255 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "NIN is required")
    @Size(min = 11, max = 11, message = "NIN must be exactly 11 characters")
    private String nin;

    @NotBlank(message = "LGA is required")
    @Size(max = 100, message = "LGA cannot exceed 100 characters")
    private String lga;

    @NotBlank(message = "Residence LGA is required")
    @Size(max = 100, message = "Residence LGA cannot exceed 100 characters")
    private String residenceLga;

    @NotBlank(message = "Gender is required")
    @Size(max = 7, message = "Gender cannot exceed 7 characters")
    private String gender;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "State of residence is required")
    @Size(max = 255, message = "State of residence cannot exceed 255 characters")
    private String stateOfResidence;

    @NotBlank(message = "State of origin is required")
    @Size(max = 255, message = "State of origin cannot exceed 255 characters")
    private String stateOfOrigin;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Specialty is required")
    @Size(max = 50, message = "Specialty cannot exceed 50 characters")
    private String specialty; // medical, education, hr, corporate_affairs, pension etc.
}
