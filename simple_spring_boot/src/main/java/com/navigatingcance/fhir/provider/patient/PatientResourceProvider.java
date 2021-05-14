package com.navigatingcance.fhir.provider.patient;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Component
public class PatientResourceProvider extends AbstractJaxRsResourceProvider<Patient> {

    @Autowired
    private PatientRecordRepository repo;

    Logger log = LoggerFactory.getLogger(PatientResourceProvider.class);

    public PatientResourceProvider(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Read
    public Patient find(@IdParam final IdType theId) {
        log.info("get patient called with {}", theId);
        Integer pid;
        try {
            pid = Integer.parseInt(theId.getIdPart());
        } catch(Exception ex) {
            log.error("patient id must be a number", ex);
            throw ex;
        }
        Optional<PatientRecord> patientRecord = repo.getPatientById(pid);
        if (patientRecord.isPresent()) {
            return patientRecord.get().toPatient();
        } else {
            throw new ResourceNotFoundException(theId);
        }
    }
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

}
