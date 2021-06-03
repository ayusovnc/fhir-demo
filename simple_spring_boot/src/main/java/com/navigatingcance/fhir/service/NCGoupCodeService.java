package com.navigatingcance.fhir.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NCGoupCodeService implements CodeService {

    Logger log = LoggerFactory.getLogger(NCGoupCodeService.class);

    private Map<String, Set<String>> groupCodes;
    private Map<String, String> groupNames;

    public NCGoupCodeService() throws Exception {
        try {
            var loincData = getLabGroups();
            groupCodes = loincData.codes;
            groupNames = loincData.names;
            log.info("Initialized NCGoupCodeService, there are {} code groups", groupCodes.size());
        } catch (Exception ex) {
            log.error("failed to load resources", ex);
            throw ex;
        }
    }


    @Override
    public Boolean isKnown(String code) {
        return groupCodes.containsKey(code);
    }

    @Override
    public Set<String> getLOINCCodes(String code) {
        if (groupCodes.containsKey(code)) {
            return groupCodes.get(code);
        } else {
            return null;
        }
    }

    @Override
    public String getName(String code) {
        if (groupNames.containsKey(code)) {
            return groupNames.get(code);
        } else {
            return null;
        }
    }
    
    record NCCodeData(Map<String, Set<String>> codes, Map<String, String> names) {};

    // First lines from NC_CODES/labs.csv:
    // ID,LOINC_CODE,CODE_SET_ID,GROUP_IDENTIFIER,COMPONENT_NAME,REFERENCE_VALUE_TYPE,UNIT,REFERENCE_RANGE_LOW,REFERENCE_RANGE_HIGH,AUTO_MAP_TITLE,AUTO_ID...
    // 1,2345-7,f0cb4fa4-43d9-4252-bade-a48f6c66e5e3,CMP,Glucose [Mass/volume] in Serum or Plasma,numeric,mg/dL,74,100,Glucose [Mass/volume] in Serum or Plasma: mg/dL,1,...
    // 2,2339-0,f0cb4fa4-43d9-4252-bade-a48f6c66e5e3,CMP,Glucose [Mass/volume] in Blood,numeric,mg/dL,74,100,Glucose [Mass/volume] in Blood: mg/dL,2,...
    // 3,74774-1,f0cb4fa4-43d9-4252-bade-a48f6c66e5e3,CMP,"Glucose [Mass/volume] in Serum, Plasma or Blood",numeric,mg/dL,74,100,"Glucose [Mass/volume] in Serum, Plasma or Blood: mg/dL",3,...
    private NCCodeData getLabGroups() throws IOException {
        Map<String, Set<String>> codes = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        CodeService.processResource("NC_CODES/labs.csv", l -> {
            String[] values = l.split(",");
            if( values.length < 8 ) {
                return;
            }
            String parent = values[3].replace('"', ' ').strip();
            String child = values[1].replace('"', ' ').strip();
            if (!codes.containsKey(parent)) {
                codes.put(parent, new HashSet<>());
            }
            codes.get(parent).add(child);
            String name = values[4].replace('"', ' ').strip();
            names.put(parent, name);
        });
        return new NCCodeData(codes, names);
    }
}
