package com.goldbyte.citieye_server.citizen_profile;

public interface CitizenProfilePublicService {
        CitizenTargetingParams getTargetingParams(String citizenCode);

        boolean profileExists(String citizenCode);

        void updateCommonHealthFields(
                        String citizenCode,
                        String bloodGroup,
                        String bloodGenotype,
                        Boolean isExpectantParent,
                        Boolean isPregnant,
                        Boolean historyOfDiabetes,
                        Boolean nhisEnrolled,
                        Boolean nhisSeniorPlanActive);
}
