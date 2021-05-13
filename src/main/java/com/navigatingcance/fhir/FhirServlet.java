package com.navigatingcance.fhir;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import lombok.extern.slf4j.Slf4j;

import com.navigatingcance.fhir.provider.*;

@Slf4j
@WebServlet("/fhir")
public class FhirServlet extends RestfulServer {

        @Override
        protected void initialize() throws ServletException {
                log.info("Initializing the servlet");

                // Create a context for the appropriate version
                setFhirContext(FhirContext.forDstu3());
                log.info("Set FHIR context");
                
                // Register resource providers
		registerProvider(new PatientResourceProvider());
                log.info("Registered providers");
                
                // Format the responses in nice HTML
		// registerInterceptor(new ResponseHighlighterInterceptor());
        }

}

