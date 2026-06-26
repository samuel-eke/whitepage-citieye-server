package com.goldbyte.citieye_server;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class CitieyeModulithTest {

    @Test
    void verifyModularity() {
        ApplicationModules.of(CitieyeServerApplication.class).verify();
    }
}
