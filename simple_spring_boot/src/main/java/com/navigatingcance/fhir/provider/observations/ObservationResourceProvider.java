package com.navigatingcance.fhir.provider.observations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Component
public class ObservationResourceProvider extends AbstractJaxRsResourceProvider<Observation> {

    Logger log = LoggerFactory.getLogger(ObservationResourceProvider.class);

    @Autowired
    private ObservationsRepository repo;

    public ObservationResourceProvider(FhirContext fhirContext) {
        super(fhirContext);
    }

    @Read
    public Observation find(@IdParam final IdType theId) {
        log.info("get observation called with {}", theId);
        Integer oid;
        try {
            oid = Integer.parseInt(theId.getIdPart());
        } catch (Exception ex) {
            log.error("patient id must be a number", ex);
            throw ex;
        }

        Optional<LabResultsRecord> labResult = repo.getLabResultsById(oid);
        if (labResult.isPresent()) {
            return labResult.get().toObservation();
        } else {
            throw new ResourceNotFoundException(theId);
        }
    }

    @Search
    public List<Observation> findPatientObservations(@RequiredParam(name = Observation.SP_SUBJECT) IdType patientId,
            @OptionalParam(name = Observation.SP_CATEGORY) TokenParam category,
            @OptionalParam(name = Observation.SP_CODE) TokenOrListParam code) {
        log.info("find observation for patient {}, category {},  codes {}", patientId, category, code);
        Integer pid;
        try {
            pid = Integer.parseInt(patientId.getIdPart());
        } catch (Exception ex) {
            log.error("patient id must be a number", ex);
            throw ex;
        }
        List<LabResultsRecord> labResult = repo.getLabResultsForPerson(pid);
        List<Observation> res = labResult.stream().map(o -> o.toObservation()).collect(Collectors.toList());

        // filter results in memory for now since this is just for one patient
        if (category != null && category.getValue() != null) {
            String catStr = category.getValue();
            Predicate<CodeableConcept> catMatch = c -> c.getCoding().stream()
                    .anyMatch(cd -> cd.hasCode() && catStr.equals(cd.getCode()));
            res = res.stream().filter(o -> o.getCategory().stream().anyMatch(catMatch)).collect(Collectors.toList());
        }
        if (code != null) {
            Set<String> wantedCodesSet = code.getValuesAsQueryTokens().stream().map(c -> c.getValue())
                    .collect(Collectors.toSet());
            res = res.stream()
                    .filter(o -> o.getCode().getCoding().stream()
                            .anyMatch(c -> c.getCode() != null && wantedCodesSet.contains(c.getCode())))
                    .collect(Collectors.toList());
        }
        return res;
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }

}
