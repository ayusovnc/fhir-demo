package com.navigatingcance.fhir.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.springframework.stereotype.Service;

@Service
public interface CodeService {
    public Boolean isKnown(String code);
    public Set<String> getLOINCCodes(String code);
    public String getName(String code);

    // Helper function to load CSVs from resources
    public static void processResource(String resName, Consumer<String> processor) throws IOException {
        InputStream is = CodeService.class.getClassLoader().getResourceAsStream(resName);
        if( resName.endsWith(".gz") ) {
            is = new GZIPInputStream(is);
        }
        InputStreamReader rd = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(rd);
        br.lines().forEach(processor);
    }

}
