package com.goldbyte.citieye_server.citizen_profile.internal;

import com.goldbyte.citieye_server.citizen_profile.internal.model.Profile;
import com.goldbyte.citieye_server.citizen_profile.internal.model.enums.DesiredSupport;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;
import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;

@Component
public class ProfileEventListener {

    @Autowired
    private ProfileRepository profileRepository;

    @EventListener
    public void onCitizenRegistered(CitizenRegisteredEvent event) {
        Profile profile = new Profile();
        profile.setCitizenCode(event.getCitizenCode());
        
        // Calculate age from date of birth
        if (event.getDateOfBirth() != null) {
            int age = Period.between(event.getDateOfBirth(), LocalDate.now()).getYears();
            profile.setAge(age);
        }

        // Map desiredSupport to Enum
        DesiredSupport supportEnum;
        try {
            supportEnum = DesiredSupport.valueOf(event.getDesiredSupport());
        } catch (IllegalArgumentException | NullPointerException e) {
            supportEnum = DesiredSupport.skill_acquisition; // Fallback default
        }
        profile.setDesiredSupport(supportEnum);
        profile.setUpdatedAt(OffsetDateTime.now());

        profileRepository.save(profile);
    }
}
