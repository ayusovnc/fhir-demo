package com.navigatingcance.fhir.service;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public interface ConfigService {
    public Boolean isLOINCPanel(String code);
    public Set<String> getLOINCPanelCodes(String code);
    public String getLOINCPanelName(String code);
}
