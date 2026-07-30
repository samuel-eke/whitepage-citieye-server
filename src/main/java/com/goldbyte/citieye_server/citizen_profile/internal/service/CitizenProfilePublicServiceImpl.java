package com.goldbyte.citieye_server.citizen_profile.internal.service;

import com.goldbyte.citieye_server.citizen_profile.CitizenProfilePublicService;
import com.goldbyte.citieye_server.citizen_profile.CitizenTargetingParams;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.goldbyte.citieye_server.citizen_profile.internal.repo.ProfileRepository;

@Service
public class CitizenProfilePublicServiceImpl implements CitizenProfilePublicService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean profileExists(String citizenCode) {
        if (citizenCode == null) {
            return false;
        }
        return profileRepository.existsById(citizenCode);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public CitizenTargetingParams getTargetingParams(String citizenCode) {
        String sql = """
            SELECT 
                u.state_of_residence AS state_residence,
                u.state_of_origin AS state_origin,
                u.date_of_birth AS date_of_birth,
                cp.cohort_id AS cohort_id
            FROM auth.users_auth u
            LEFT JOIN citizen_profile.profiles p ON u.citizen_code = p.citizen_code
            LEFT JOIN cohort_placement.placements cp ON u.citizen_code = cp.citizen_code
            WHERE u.citizen_code = :citizenCode
            """;

        List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("citizenCode", citizenCode)
                .getResultList();

        if (results.isEmpty()) {
            return new CitizenTargetingParams(null, null, null, null);
        }

        Tuple tuple = results.get(0);
        String stateResidence = tuple.get("state_residence") != null ? String.valueOf(tuple.get("state_residence")) : null;
        String stateOrigin = tuple.get("state_origin") != null ? String.valueOf(tuple.get("state_origin")) : null;
        Integer cohortId = tuple.get("cohort_id") != null ? ((Number) tuple.get("cohort_id")).intValue() : null;
        java.time.LocalDate dateOfBirth = tuple.get("date_of_birth") != null ? tuple.get("date_of_birth", java.time.LocalDate.class) : null;
        
        return new CitizenTargetingParams(cohortId, stateResidence, stateOrigin, dateOfBirth);
    }
}
