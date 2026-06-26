package com.goldbyte.citieye_server.cohort_placement.internal;

import com.goldbyte.citieye_server.cohort_placement.internal.model.Cohort;
import com.goldbyte.citieye_server.cohort_placement.internal.model.Placement;
import com.goldbyte.citieye_server.cohort_placement.internal.repo.CohortRepository;
import com.goldbyte.citieye_server.cohort_placement.internal.repo.PlacementRepository;
import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;

@Component
public class CohortPlacementEventListener {

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private PlacementRepository placementRepository;

    @EventListener
    public void onCitizenRegistered(CitizenRegisteredEvent event) {
        if (event.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Citizen date of birth is required for cohort placement");
        }

        int age = Period.between(event.getDateOfBirth(), LocalDate.now()).getYears();

        List<Cohort> matchingCohorts = cohortRepository.findMatchingCohorts(age);
        if (matchingCohorts.isEmpty()) {
            throw new IllegalStateException("No cohort placement matches age: " + age);
        }

        Cohort cohort = matchingCohorts.get(0);

        Placement placement = new Placement();
        placement.setCitizenCode(event.getCitizenCode());
        placement.setCohort(cohort);
        placement.setAgeAtRegistration(age);
        placement.setLastTransitionedAt(OffsetDateTime.now());
        placement.setCreatedAt(OffsetDateTime.now());

        placementRepository.save(placement);

        // Mutate event to return cohort name back to the userauth module
        event.setCohortName(cohort.getName());
    }
}
