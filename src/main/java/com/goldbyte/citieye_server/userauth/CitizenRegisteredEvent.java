package com.goldbyte.citieye_server.userauth;

import java.time.LocalDate;

/**
 * Spring Modulith Application Event published when a citizen registers.
 * Placed in the public root package of the userauth module.
 */
public class CitizenRegisteredEvent {
    private final String citizenCode;
    private final LocalDate dateOfBirth;
    private final String gender;

    // Mutated by the cohort placement listener to return cohort name back to userauth
    private String cohortName;

    public CitizenRegisteredEvent(String citizenCode, LocalDate dateOfBirth, String gender) {
        this.citizenCode = citizenCode;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public String getCitizenCode() {
        return citizenCode;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getCohortName() {
        return cohortName;
    }

    public void setCohortName(String cohortName) {
        this.cohortName = cohortName;
    }
}
