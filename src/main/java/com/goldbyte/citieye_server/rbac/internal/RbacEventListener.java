package com.goldbyte.citieye_server.rbac.internal;

import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener in the rbac module that listens to FetchUserAuthoritiesEvent
 * to populate granular authorities for a given user role.
 * Maintains modularity by decoupling userauth module from rbac internals.
 */
@Component
public class RbacEventListener {

    @EventListener
    public void onFetchUserAuthorities(FetchUserAuthoritiesEvent event) {
        if (event.getRole() == null) {
            return;
        }

        try {
            // Find role in rbac's UserRole enum (case-insensitive)
            UserRole rbacRole = UserRole.valueOf(event.getRole().trim().toLowerCase());
            
            // Add authorities based on the role (with inheritance)
            event.addAuthorities(rbacRole.getInheritedPermissions());
        } catch (IllegalArgumentException e) {
            // Role not found or invalid
        }
    }
}
