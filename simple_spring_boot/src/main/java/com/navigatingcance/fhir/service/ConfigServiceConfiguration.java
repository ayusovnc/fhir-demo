package com.navigatingcance.fhir.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigServiceConfiguration {

    @Value("${configService.inmem}")
    private Boolean useInMemoryDb;

    @Bean(name = "LOINCPanels")
    public CodeService  getLOINCConfigService() throws Exception {
        if( useInMemoryDb ) {
            return new InMemLOINCPanelsService();
        } else {
            return new DbLOINCPanelsService();
        }
    }   

    @Bean(name = "NCGroups")
    public CodeService  getNCConfigService() throws Exception {
        if( useInMemoryDb ) {
            return new NCGoupCodeService();
        } else {
            throw new RuntimeException("NC Codes DB service is not supported yet");
        }
    }   

}
