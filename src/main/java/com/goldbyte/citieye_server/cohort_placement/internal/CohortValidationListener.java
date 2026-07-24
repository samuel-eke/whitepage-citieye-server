package com.goldbyte.citieye_server.cohort_placement.internal;

import com.goldbyte.citieye_server.admin_content_cms.ValidateTargetingEvent;
import com.goldbyte.citieye_server.cohort_placement.internal.repo.CohortRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CohortValidationListener {
    private final CohortRepository cohortRepository;

    public CohortValidationListener(CohortRepository cohortRepository) {
        this.cohortRepository = cohortRepository;
    }

    @EventListener
    public void onValidateTargeting(ValidateTargetingEvent event) {
        if (event.getCohortId() != null) {
            boolean exists = cohortRepository.existsById(event.getCohortId());
            if (!exists) {
                event.setCohortValid(false);
            }
        }
    }
}
