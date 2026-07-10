package com.goldbyte.citieye_server.userauth;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Modulith Application Event published to retrieve granular authorities 
 * for a user based on their role and other parameters.
 * Placed in the public root package of the userauth module to maintain modularity.
 */
public class FetchUserAuthoritiesEvent {
    private final String role;
    private final String citizenCode;
    private final List<String> authorities = new ArrayList<>();

    public FetchUserAuthoritiesEvent(String role, String citizenCode) {
        this.role = role;
        this.citizenCode = citizenCode;
    }

    public String getRole() {
        return role;
    }

    public String getCitizenCode() {
        return citizenCode;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void addAuthority(String authority) {
        this.authorities.add(authority);
    }

    public void addAuthorities(List<String> authorities) {
        if (authorities != null) {
            this.authorities.addAll(authorities);
        }
    }
}
