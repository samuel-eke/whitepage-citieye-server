package com.goldbyte.citieye_server.userauth.internal.model;

import com.goldbyte.citieye_server.userauth.internal.model.enums.GenderType;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianState;
import com.goldbyte.citieye_server.userauth.internal.model.enums.NigerianStateConverter;
import com.goldbyte.citieye_server.userauth.internal.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "admin_user", schema = "auth", indexes = {
        @Index(name = "idx_admin_user_state_origin", columnList = "state_of_origin"),
        @Index(name = "idx_admin_user_role", columnList = "role")
}, uniqueConstraints = {
        @UniqueConstraint(name = "admin_user_phone_number_key", columnNames = {"phone_number"}),
        @UniqueConstraint(name = "admin_user_email_key", columnNames = {"email"}),
        @UniqueConstraint(name = "admin_user_nin_key", columnNames = {"nin"}),
        @UniqueConstraint(name = "admin_user_citizen_code_key", columnNames = {"citizen_code"})
})
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 11)
    @NotNull
    @Column(name = "phone_number", nullable = false, length = 11)
    private String phoneNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 11)
    @NotNull
    @Column(name = "nin", nullable = false, length = 11)
    private String nin;
    @Size(max = 30)
    @Column(name = "citizen_code", length = 30)
    private String citizenCode;

    @NotNull
    @Column(name = "address", nullable = false, length = Integer.MAX_VALUE)
    private String address;
    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @NotNull
    @Convert(converter = NigerianStateConverter.class)
    @Column(name = "state_of_origin", nullable = false, columnDefinition = "nigerian_state")
    private NigerianState stateOfOrigin;

    @NotNull
    @Convert(converter = NigerianStateConverter.class)
    @Column(name = "state_of_residence", nullable = false, columnDefinition = "nigerian_state")
    private NigerianState stateOfResidence;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, columnDefinition = "gender_type")
    private GenderType gender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    private UserRole role;
}