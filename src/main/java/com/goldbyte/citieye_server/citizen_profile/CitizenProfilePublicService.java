package com.goldbyte.citieye_server.citizen_profile;

public interface CitizenProfilePublicService {
    CitizenTargetingParams getTargetingParams(String citizenCode);
    boolean profileExists(String citizenCode);
}
