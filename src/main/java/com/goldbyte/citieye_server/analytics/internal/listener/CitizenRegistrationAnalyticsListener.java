package com.goldbyte.citieye_server.analytics.internal.listener;

import com.goldbyte.citieye_server.analytics.internal.repo.AnalyticsRepository;
import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import com.goldbyte.citieye_server.health.HealthRecordLoggedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Analytics module listener that reacts to application events (citizen
 * registrations,
 * logged health records) to refresh materialized views.
 *
 * <p>
 * The refresh is performed asynchronously so it does not block or delay
 * the citizen registration or health record API response. Spring Modulith
 * boundary rules
 * are respected — only the public event classes are imported.
 * </p>
 */
@Component
public class CitizenRegistrationAnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(CitizenRegistrationAnalyticsListener.class);

    @Autowired
    private AnalyticsRepository analyticsRepository;

    /**
     * Triggered after a citizen is registered. Asynchronously refreshes all
     * analytics materialized views to keep dashboard data current.
     */
    @Async
    @EventListener
    public void onCitizenRegistered(CitizenRegisteredEvent event) {
        log.info("CitizenRegistrationAnalyticsListener: new citizen [{}] registered — refreshing analytics views.",
                event.getCitizenCode());
        refreshViews(event.getCitizenCode(),
                "Failed to refresh analytics materialized views after citizen registration");
    }

    /**
     * Triggered after a health record is logged. Asynchronously refreshes all
     * analytics materialized views to keep dashboard data current.
     */
    @Async
    @EventListener
    public void onHealthRecordLogged(HealthRecordLoggedEvent event) {
        log.info(
                "CitizenRegistrationAnalyticsListener: new health record [{}] logged for citizen [{}] — refreshing analytics views.",
                event.getRecordId(), event.getCitizenCode());
        refreshViews(event.getCitizenCode(),
                "Failed to refresh analytics materialized views after health record logging");
    }

    private void refreshViews(String contextCode, String errorMsgPrefix) {
        try {
            analyticsRepository.refreshAllMaterializedViews();
        } catch (Exception e) {
            log.error(errorMsgPrefix + " [{}]: {}", contextCode, e.getMessage(), e);
        }
    }
}
