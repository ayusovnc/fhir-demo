package com.navigatingcance.fhir.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigServiceConfiguration {

    @Value("${configService.inmem}")
    private Boolean useInMemoryDb;

    @Bean
    public CodeService  getConfigService() throws Exception {
        if( useInMemoryDb ) {
            return new InMemLOINCPanelsService();
        } else {
            return new DbLOINCPanelsService();
        }
    }   

}
