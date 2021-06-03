package com.navigatingcance.fhir.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemLOINCPanelsService implements CodeService {

    Logger log = LoggerFactory.getLogger(InMemLOINCPanelsService.class);

    private Map<String, Set<String>> panelCodes;
    private Map<String, String> panelNames;

    public InMemLOINCPanelsService() throws Exception {
        try {
            var loincData = getLOINCPanels();
            panelCodes = loincData.codes;
            panelNames = loincData.names;
            log.info("Initialized InMemConfigService, there are {} panels", panelCodes.size());
        } catch (Exception ex) {
            log.error("failed to load resources", ex);
            throw ex;
        }
    }

    @Override
    public Boolean isKnown(String code) {
        return panelCodes.containsKey(code);
    }

    @Override
    public Set<String> getLOINCCodes(String code) {
        if (panelCodes.containsKey(code)) {
            return panelCodes.get(code);
        } else {
            return null;
        }
    }

    @Override
    public String getName(String code) {
        if (panelNames.containsKey(code)) {
            return panelNames.get(code);
        } else {
            return null;
        }
    }

    record LOINCData(Map<String, Set<String>> codes, Map<String, String> names) {
    };

    // First lines from PanelsAndForms.csv:
    // "ParentId","ParentLoinc","ParentName","ID","SEQUENCE","Loinc","LoincName",...
    // "10142","13361-1","Semen Analysis Pnl","10142","1","13361-1","Semen Analysis Pnl",...
    private LOINCData getLOINCPanels() throws IOException {
        Map<String, Set<String>> codes = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        CodeService.processResource("LOINC/PanelsAndForms.csv.gz", l -> {
            String[] values = l.split(",");
            String parent = values[1].replace('"', ' ').strip();
            String child = values[5].replace('"', ' ').strip();
            if (!codes.containsKey(parent)) {
                codes.put(parent, new HashSet<>());
            }
            codes.get(parent).add(child);
            String name = values[2].replace('"', ' ').strip();
            names.put(parent, name);
        });
        return new LOINCData(codes, names);
    }

}
