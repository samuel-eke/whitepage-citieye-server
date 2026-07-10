package com.goldbyte.citieye_server.rbac.internal;

import com.goldbyte.citieye_server.userauth.FetchUserAuthoritiesEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRoleHierarchyTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RoleHierarchy roleHierarchy;

    @Test
    void testUserRolePermissionInheritance() {
        // Direct permissions of super_admin
        assertTrue(UserRole.super_admin.getPermissions().contains("MANAGE_USERS"));
        assertFalse(UserRole.super_admin.getPermissions().contains("PUBLISH_CMS"));

        // Inherited permissions of super_admin (should include agency_admin's)
        List<String> inherited = UserRole.super_admin.getInheritedPermissions();
        assertTrue(inherited.contains("MANAGE_USERS"));
        assertTrue(inherited.contains("PUBLISH_CMS"));
        assertTrue(inherited.contains("READ_CITIZENS"));
        assertTrue(inherited.contains("VIEW_ANALYTICS"));
    }

    @Test
    void testRbacEventListenerInheritance() {
        FetchUserAuthoritiesEvent event = new FetchUserAuthoritiesEvent("super_admin", "cit-adm-123456");
        eventPublisher.publishEvent(event);

        List<String> authorities = event.getAuthorities();
        assertTrue(authorities.contains("MANAGE_USERS"));
        assertTrue(authorities.contains("PUBLISH_CMS")); // Inherited
        assertTrue(authorities.contains("READ_CITIZENS")); // Inherited
    }

    @Test
    void testSpringSecurityRoleHierarchy() {
        // super_admin should imply agency_admin
        Collection<? extends GrantedAuthority> reachable = roleHierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("super_admin"))
        );

        assertTrue(reachable.stream().anyMatch(a -> a.getAuthority().equals("super_admin")));
        assertTrue(reachable.stream().anyMatch(a -> a.getAuthority().equals("agency_admin")));
        assertTrue(reachable.stream().anyMatch(a -> a.getAuthority().equals("field_officer")));
        assertTrue(reachable.stream().anyMatch(a -> a.getAuthority().equals("citizen")));
    }
}
