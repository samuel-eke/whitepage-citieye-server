package com.goldbyte.citieye_server.userauth.internal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Comprehensive login response DTO containing auth data, citizen profile,
 * and cohort placement information.
 *
 * Null fields are omitted from the JSON response to keep it clean.
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CitizenLoginResponse {

    // === Authentication Data ===
    private Long id;
    private String email;
    private String phoneNumber;
    private String nin;
    private String citizenCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String address;
    private String stateOfResidence;
    private String stateOfOrigin;
    private String role;
    private LocalDate dateOfBirth;
    private OffsetDateTime createdAt;

    // === Profile Data ===
    private Integer age;
    private String lga;
    private String hometown;
    private String currentEducationalLevel;
    private String highestEducationalQualification;
    private String courseOfStudy;
    private String universityAttended;
    private String degreeAttained;
    private Integer yearOfGraduation;
    private Boolean isExpectantParent;
    private Integer householdSize;
    private Boolean isPregnant;
    private String bloodGroup;
    private String bloodGenotype;
    private String currentEmploymentStatus;
    private String placeOfEmploymentBusiness;
    private String nameOfEmployerBusiness;
    private String natureOfBusinessJob;
    private String desiredSupport;

    // === Cohort Placement Data ===
    private String cohortName;
    private String cohortDescription;
    private Integer ageAtRegistration;

    // === Meta ===
    private String message;
    // this is for jwt and other tokens
    private String data;
}
