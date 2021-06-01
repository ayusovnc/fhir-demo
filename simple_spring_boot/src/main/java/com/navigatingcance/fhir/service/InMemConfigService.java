package com.navigatingcance.fhir.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemConfigService implements ConfigService {

    Logger log = LoggerFactory.getLogger(InMemConfigService.class);

    private Map<String,Set<String>> panelCodes;

    public InMemConfigService() throws Exception {
        try {
            panelCodes = getLOINCPanels();
            log.info("Initialized InMemConfigService, there are {} panels", panelCodes.size());
        } catch(Exception ex) {
            log.error("failed to load resources", ex);
            throw ex;
        }
    }

    @Override
    public Boolean isLOINCPanel(String code) {
        return panelCodes.containsKey(code);
    }

    @Override
    public List<String> getLOINCPanelCodes(String code) {
        if( panelCodes.containsKey(code) ) {
            return panelCodes.get(code).stream().collect(Collectors.toList());
        } else {
            return null;
        }
    }

    // First lines from PanelsAndForms.csv:
    // "ParentId","ParentLoinc","ParentName","ID","SEQUENCE","Loinc","LoincName",...
    // "10142","13361-1","Semen Analysis Pnl","10142","1","13361-1","Semen Analysis Pnl",...
    private Map<String,Set<String>> getLOINCPanels () throws IOException {
        Map<String,Set<String>> res = new HashMap<>();
        processResource("LOINC/PanelsAndForms.csv.gz", l -> {
            String[] values = l.split(",");
            String parent = values[1].replace('"', ' ').strip();
            String child = values[5].replace('"', ' ').strip();
            if( !res.containsKey(parent) ) {
                res.put(parent, new HashSet<>());
            }
            res.get(parent).add(child);
        }) ;
        return res;
    }

    private void processResource(String resName, Consumer<String> processor) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resName);
        is = new GZIPInputStream(is);
        InputStreamReader rd =  new InputStreamReader(is);
        BufferedReader br = new BufferedReader(rd);
        br.lines().forEach(processor);;
    }

}
