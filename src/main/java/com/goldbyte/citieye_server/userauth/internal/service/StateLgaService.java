package com.goldbyte.citieye_server.userauth.internal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class StateLgaService {
    private static final Logger log = LoggerFactory.getLogger(StateLgaService.class);

    private Map<String, List<String>> statesAndLgas = new TreeMap<>();

    @PostConstruct
    public void init() {
        try {
            log.info("Loading states and LGAs from JSON file...");
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("states_and_lga.json").getInputStream();
            List<Map<String, Object>> list = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
            
            Map<String, List<String>> tempMap = new TreeMap<>();
            for (Map<String, Object> stateMap : list) {
                String stateName = (String) stateMap.get("state");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> lgasObj = (List<Map<String, Object>>) stateMap.get("lgas");
                List<String> lgaNames = new ArrayList<>();
                if (lgasObj != null) {
                    for (Map<String, Object> lgaMap : lgasObj) {
                        String lgaName = (String) lgaMap.get("name");
                        if (lgaName != null) {
                            lgaNames.add(lgaName);
                        }
                    }
                }
                // Sort LGA names alphabetically
                Collections.sort(lgaNames);
                tempMap.put(stateName, lgaNames);
            }
            this.statesAndLgas = tempMap;
            log.info("Successfully loaded {} states and their LGAs.", statesAndLgas.size());
        } catch (Exception e) {
            log.error("Failed to load states and LGAs dynamic data", e);
            // Fallback to empty map to prevent startup failure
            this.statesAndLgas = new TreeMap<>();
        }
    }

    public Map<String, List<String>> getStatesAndLgas() {
        return statesAndLgas;
    }
}
