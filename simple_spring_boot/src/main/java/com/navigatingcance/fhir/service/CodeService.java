package com.navigatingcance.fhir.service;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public interface CodeService {
    public Boolean isKnown(String code);
    public Set<String> getLOINCCodes(String code);
    public String getName(String code);
}
