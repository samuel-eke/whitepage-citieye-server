package com.goldbyte.citieye_server.education.internal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Entity representing a school institution logged by educational officers.
 * Holds school name, LGA, and total pupil count.
 */
@Getter
@Setter
@Entity
@Table(name = "schools", schema = "education")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull
    @Size(max = 100)
    @Column(name = "lga", nullable = false, length = 100)
    private String lga;

    @Size(max = 50)
    @Column(name = "state", length = 50)
    private String state;

    @NotNull
    @Positive(message = "Total pupil count must be greater than zero")
    @Column(name = "pupil_count", nullable = false)
    private Integer pupilCount;

    @Size(max = 50)
    @Column(name = "school_type", length = 50)
    private String schoolType; // PRIMARY, SECONDARY

    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
