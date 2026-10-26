package com.goldbyte.citieye_server.userauth;

import java.util.Optional;

/**
 * Public service interface exposed by the {@code userauth} module to other application modules.
 * Complies with Spring Modulith module boundaries by providing cross-module access without
 * leaking internal models or repositories.
 */
public interface UserAuthPublicService {

    /**
     * Resolves the state of residence for an officer or administrative user by citizen/officer code.
     *
     * @param officerCode the citizen code of the officer
     * @return an Optional containing the formatted state name (e.g., "Edo", "Cross River"),
     *         or empty if not found or no state is set
     */
    Optional<String> getOfficerStateOfResidence(String officerCode);
}
