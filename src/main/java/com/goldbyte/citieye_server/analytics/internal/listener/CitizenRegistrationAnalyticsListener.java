package com.goldbyte.citieye_server.analytics.internal.listener;

import com.goldbyte.citieye_server.analytics.internal.repo.AnalyticsRepository;
import com.goldbyte.citieye_server.userauth.CitizenRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Analytics module listener that reacts to {@link CitizenRegisteredEvent}
 * to refresh materialized views after each new citizen registration.
 *
 * <p>The refresh is performed asynchronously so it does not block or delay
 * the citizen registration API response. Spring Modulith boundary rules
 * are respected — only the public event class from the {@code userauth}
 * module is imported; no internal packages are touched.</p>
 */
@Component
public class CitizenRegistrationAnalyticsListener {

    private static final Logger log = LoggerFactory.getLogger(CitizenRegistrationAnalyticsListener.class);

    @Autowired
    private AnalyticsRepository analyticsRepository;

    /**
     * Triggered after a citizen is registered. Asynchronously refreshes all
     * analytics materialized views to keep dashboard data current.
     *
     * @param event the citizen registration event carrying the new citizen's code
     */
    @Async
    @EventListener
    public void onCitizenRegistered(CitizenRegisteredEvent event) {
        log.info("CitizenRegistrationAnalyticsListener: new citizen [{}] registered — refreshing analytics views.",
                event.getCitizenCode());
        try {
            analyticsRepository.refreshAllMaterializedViews();
        } catch (Exception e) {
            // Log and swallow — a view refresh failure must never disrupt the registration flow
            log.error("Failed to refresh analytics materialized views after citizen registration [{}]: {}",
                    event.getCitizenCode(), e.getMessage(), e);
        }
    }
}
