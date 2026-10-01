package com.goldbyte.citieye_server.userauth.internal.dto;

import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Data transfer object conveying registered field officer information to administrators.
 * Sensitive fields (password hashes, raw NIN, refresh tokens) are deliberately excluded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerSummaryDto {
    private Long id;
    private String citizenCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String specialty;
    private String gender;
    private String stateOfResidence;
    private String residenceLga;
    private String stateOfOrigin;
    private String lga;
    private String address;
    private LocalDate dateOfBirth;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static OfficerSummaryDto fromEntity(AdminUser user) {
        if (user == null) {
            return null;
        }

        String fullName = String.format("%s %s%s",
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getMiddleName() != null && !user.getMiddleName().isBlank() ? user.getMiddleName() + " " : "",
                user.getLastName() != null ? user.getLastName() : ""
        ).replaceAll("\\s+", " ").trim();

        return OfficerSummaryDto.builder()
                .id(user.getId())
                .citizenCode(user.getCitizenCode())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(fullName)
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .specialty(user.getSpecialty())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .stateOfResidence(user.getStateOfResidence() != null ? user.getStateOfResidence().name() : null)
                .residenceLga(user.getResidenceLga())
                .stateOfOrigin(user.getStateOfOrigin() != null ? user.getStateOfOrigin().name() : null)
                .lga(user.getLga())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
