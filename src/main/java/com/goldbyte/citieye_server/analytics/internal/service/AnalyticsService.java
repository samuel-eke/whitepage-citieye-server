package com.goldbyte.citieye_server.analytics.internal.service;

import com.goldbyte.citieye_server.analytics.internal.dto.*;
import com.goldbyte.citieye_server.analytics.internal.repo.AnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for the analytics dashboard.
 * Orchestrates calls to {@link AnalyticsRepository} and assembles composite response DTOs.
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private AnalyticsRepository analyticsRepository;

    /**
     * Returns the full analytics summary dashboard response —
     * headline metrics, state/LGA breakdown, cohort breakdown, employment and health snapshots.
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getSummary() {
        long totalCitizens          = analyticsRepository.countTotalCitizens();
        long last7Days              = analyticsRepository.countCitizensRegisteredInLastDays(7);
        long last30Days             = analyticsRepository.countCitizensRegisteredInLastDays(30);
        long maleCitizens           = analyticsRepository.countMaleCitizens();
        long femaleCitizens         = analyticsRepository.countFemaleCitizens();

        List<StateBreakdownDto>      byStateAndLga      = analyticsRepository.findStateAndLgaBreakdown();
        List<CohortBreakdownDto>     byCohort           = analyticsRepository.findCohortBreakdown();
        List<EmploymentBreakdownDto> byEmploymentStatus  = analyticsRepository.findEmploymentBreakdown();
        HealthIndicatorsSummaryDto   healthIndicators    = analyticsRepository.findHealthIndicatorsSummary();

        return new AnalyticsSummaryDto(
                totalCitizens,
                last7Days,
                last30Days,
                maleCitizens,
                femaleCitizens,
                byStateAndLga,
                byCohort,
                byEmploymentStatus,
                healthIndicators
        );
    }

    /**
     * Returns a paginated list of citizens filtered by optional state, LGA, gender, and cohort.
     *
     * @param stateOfOrigin optional filter by state of origin
     * @param lga           optional partial-match filter by LGA
     * @param gender        optional filter by gender ('male' or 'female')
     * @param cohortName    optional partial-match filter by cohort name
     * @param page          zero-based page index
     * @param size          number of results per page (defaults to 20 if null or zero)
     */
    @Transactional(readOnly = true)
    public PagedResult<CitizenListItemDto> listCitizens(String stateOfOrigin, String lga,
                                                        String gender, String cohortName,
                                                        Integer page, Integer size) {
        int pageNum  = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0)  ? size : DEFAULT_PAGE_SIZE;

        List<CitizenListItemDto> content = analyticsRepository.searchCitizens(
                stateOfOrigin, lga, gender, cohortName, pageNum, pageSize);
        long total = analyticsRepository.countSearchCitizens(stateOfOrigin, lga, gender, cohortName);

        return PagedResult.of(content, pageNum, pageSize, total);
    }

    /**
     * Returns the full profile detail for a single citizen, including all progressive profile fields.
     *
     * @param citizenCode the unique citizen code
     * @return an Optional containing the profile DTO, or empty if the citizen is not found
     */
    @Transactional(readOnly = true)
    public Optional<CitizenProfileAnalyticsDto> getCitizenProfileDetail(String citizenCode) {
        return analyticsRepository.findCitizenProfileDetail(citizenCode);
    }

    /**
     * Triggers a full refresh of all analytics materialized views.
     * This is a potentially expensive operation — call sparingly or on a schedule.
     */
    @Transactional
    public void refreshViews() {
        log.info("Manual analytics view refresh triggered.");
        analyticsRepository.refreshAllMaterializedViews();
    }
}
