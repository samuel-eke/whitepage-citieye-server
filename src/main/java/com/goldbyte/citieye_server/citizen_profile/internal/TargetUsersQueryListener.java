package com.goldbyte.citieye_server.citizen_profile.internal;

import com.goldbyte.citieye_server.notification.QueryTargetUsersEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Listener that handles QueryTargetUsersEvent by querying the database for matching
 * citizen codes across users_auth, profiles, and placements.
 */
@Component
public class TargetUsersQueryListener {

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener
    @SuppressWarnings("unchecked")
    public void onQueryTargetUsers(QueryTargetUsersEvent event) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.citizen_code FROM auth.users_auth u ");
        sql.append("LEFT JOIN citizen_profile.profiles p ON u.citizen_code = p.citizen_code ");
        sql.append("LEFT JOIN cohort_placement.placements cp ON u.citizen_code = cp.citizen_code ");
        // Ensure we only target users who are citizens
        sql.append("WHERE u.role = 'citizen' ");

        if (event.getCohortId() != null) {
            sql.append("AND cp.cohort_id = :cohortId ");
        }
        if (event.getStateResidence() != null) {
            sql.append("AND u.state_of_residence = :stateResidence::auth.nigerian_state ");
        }
        if (event.getStateOrigin() != null) {
            sql.append("AND u.state_of_origin = :stateOrigin::auth.nigerian_state ");
        }
        Query query = entityManager.createNativeQuery(sql.toString());

        if (event.getCohortId() != null) {
            query.setParameter("cohortId", event.getCohortId());
        }
        if (event.getStateResidence() != null) {
            String dbStateRes = event.getStateResidence().replace('_', ' ');
            query.setParameter("stateResidence", dbStateRes);
        }
        if (event.getStateOrigin() != null) {
            String dbStateOrig = event.getStateOrigin().replace('_', ' ');
            query.setParameter("stateOrigin", dbStateOrig);
        }

        List<String> results = query.getResultList();
        event.addCitizenCodes(results);
    }
}
