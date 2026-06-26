package com.goldbyte.citieye_server.citizen_profile.internal.model;

import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.DesiredSupport;
import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.EmploymentStatus;
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
@Table(name = "profiles", schema = "citizen_profile")
public class Profile {

    @Id
    @Size(max = 30)
    @Column(name = "citizen_code", nullable = false, length = 30)
    private String citizenCode;

    @Size(max = 100)
    @Column(name = "lga", length = 100)
    private String lga;

    @Size(max = 100)
    @Column(name = "hometown", length = 100)
    private String hometown;

    @Size(max = 100)
    @Column(name = "current_educational_level", length = 100)
    private String currentEducationalLevel;

    @Size(max = 100)
    @Column(name = "highest_educational_qualification", length = 100)
    private String highestEducationalQualification;

    @Size(max = 100)
    @Column(name = "course_of_study", length = 100)
    private String courseOfStudy;

    @Column(name = "age")
    private Integer age;

    @Size(max = 255)
    @Column(name = "university_attended", length = 255)
    private String universityAttended;

    @Size(max = 100)
    @Column(name = "degree_attained", length = 100)
    private String degreeAttained;

    @Column(name = "year_of_graduation")
    private Integer yearOfGraduation;

    @Column(name = "is_expectant_parent")
    private Boolean isExpectantParent;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "is_pregnant")
    private Boolean isPregnant;

    @Size(max = 5)
    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Size(max = 5)
    @Column(name = "blood_genotype", length = 5)
    private String bloodGenotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_employment_status", columnDefinition = "employment_status_type")
    private EmploymentStatus currentEmploymentStatus;

    @Size(max = 255)
    @Column(name = "place_of_employment_business", length = 255)
    private String placeOfEmploymentBusiness;

    @Size(max = 255)
    @Column(name = "name_of_employer_business", length = 255)
    private String nameOfEmployerBusiness;

    @Size(max = 255)
    @Column(name = "nature_of_business_job", length = 255)
    private String natureOfBusinessJob;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "desired_support", nullable = false, columnDefinition = "desired_support_type")
    private DesiredSupport desiredSupport;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
