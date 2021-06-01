package com.navigatingcance.fhir.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface ConfigService {
    public Boolean isLOINCPanel(String code);
    public List<String> getLOINCPanelCodes(String code);
}
