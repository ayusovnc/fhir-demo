package com.navigatingcance.fhir.provider.observations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.navigatingcance.fhir.service.CodeService;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("LOINCPanels")
    private CodeService LOINCPanelsService;

    @Autowired
    @Qualifier("NCGroups")
    private CodeService NCGroupsService;

    public ObservationResourceProvider(FhirContext fhirContext) {
        super(fhirContext);
    }

    static ObservationCategory groupNameToObsCategory(String group_identifier) {
        if (group_identifier == null || group_identifier.isBlank()) {
            return ObservationCategory.NULL;
        }
        group_identifier = group_identifier.toLowerCase();
        if (group_identifier.contains("vital signs")) {
            return ObservationCategory.VITALSIGNS;
        }
        return ObservationCategory.LABORATORY;
    }

    private void setObservationValue(LabResultsRecord rec, Object res) {
        // Value
        // https://www.hl7.org/fhir/observation-definitions.html#Observation.component.value_x_
        Type value;
        try {
            Quantity q = new Quantity();
            q.setValue(new BigDecimal(rec.quantity()));
            q.setCode(rec.unit());
            value = q;
        } catch (Exception ex) {
            StringType ts = new StringType(rec.quantity());
            value = ts;
        }

        // Coding
        // https://www.hl7.org/fhir/observation-definitions.html#Observation.code
        CodeableConcept code = new CodeableConcept();
        code.addCoding().setCode(rec.loinc_code()).setSystem("http://loinc.org");
        code.setTextElement(new StringType(rec.component_name()));

        if (res instanceof Observation o) {
            o.setValue(value);
            o.setCode(code);

            // reference range
            // https://www.hl7.org/fhir/observation-definitions.html#Observation.referenceRange
            if (rec.normal_range_min() != null) {
                o.addReferenceRange().setLow(new Quantity(rec.normal_range_min()));
            }
            if (rec.normal_range_max() != null) {
                o.addReferenceRange().setHigh(new Quantity(rec.normal_range_max()));
            }

            // interpretation
            // https://www.hl7.org/fhir/observation-definitions.html#Observation.interpretation
            o.addInterpretation().setText(rec.interpretation_concept());

        } else if (res instanceof ObservationComponentComponent o) {
            o.setValue(value);
            o.setCode(code);

            // reference range
            // https://www.hl7.org/fhir/observation-definitions.html#Observation.referenceRange
            if (rec.normal_range_min() != null) {
                o.addReferenceRange().setLow(new Quantity(rec.normal_range_min()));
            }
            if (rec.normal_range_max() != null) {
                o.addReferenceRange().setHigh(new Quantity(rec.normal_range_max()));
            }

            // interpretation
            // https://www.hl7.org/fhir/observation-definitions.html#Observation.interpretation
            o.addInterpretation().setText(rec.interpretation_concept());
        }

    }

    Observation recordToBaseObservation(LabResultsRecord rec, String panelCode, boolean ncCode) {
        Observation res = new Observation();
        res.setId(rec.id().toString());
        res.setSubject(new Reference("Patient/" + rec.person_id().toString())); // TODO. May be wrong. Person != Patient
        res.setStatus(ObservationStatus.FINAL);

        res.setEffective(new DateTimeType(rec.performed_on()));

        if (panelCode != null) {
            // Panel coding, special case of coding
            // Example: https://www.hl7.org/fhir/observation-example-bloodpressure.html
            CodeableConcept code = new CodeableConcept();
            code.setTextElement(new StringType(rec.component_name()));
            Coding cd = code.addCoding();
            cd.setCode(panelCode);
            String displayName;
            if (ncCode) {
                displayName = NCGroupsService.getName(panelCode);
                cd.setSystem("http://navigatingcare.com");
            } else {
                displayName = LOINCPanelsService.getName(panelCode);
                cd.setSystem("http://loinc.org");
            }
            cd.setDisplay(displayName);
            res.setCode(code);
        }

        CodeableConcept cat = res.addCategory().setText(rec.group_identifier());

        ObservationCategory obsCat = groupNameToObsCategory(rec.group_identifier());
        if (obsCat != ObservationCategory.NULL) {
            Coding groupCoding = cat.addCoding();
            groupCoding.setSystem(obsCat.getSystem());
            groupCoding.setCode(obsCat.toCode());
        }
        return res;
    }

    // If not a panel
    Observation recordToObservation(LabResultsRecord rec) {
        Observation res = recordToBaseObservation(rec, null, false);
        setObservationValue(rec, res);
        return res;
    }

    // Create one single observation record for all LOINC codes on the panel
    List<Observation> labResultsToPanels(List<LabResultsRecord> alllabResult, String panel, Set<String> codes, boolean ncCode) {
        // Just the lab results that match the panel, grouped by date.
        // TODO. The expectation is that the panel observation are all on the same date.
        // Is that correct?
        // TODO. Possibly need to use encounter in here to group obserations or some
        // othe better way.
        Map<Date, List<LabResultsRecord>> labsByDate = alllabResult.stream().filter(l -> codes.contains(l.loinc_code()))
                .collect(Collectors.groupingBy(LabResultsRecord::performed_on));
        log.debug("distinct {} lab dates for panel {} {}", labsByDate.size(), panel, codes);
        LinkedList<Observation> res = new LinkedList<>();
        for (Date dateTaken : labsByDate.keySet()) {
            List<LabResultsRecord> labResultsOnTheDay = labsByDate.get(dateTaken);
            // There must be at least one lab record by construction
            Observation panelObservation = recordToBaseObservation(labResultsOnTheDay.get(0), panel, ncCode);
            for (LabResultsRecord singleLabResult : labResultsOnTheDay) {
                ObservationComponentComponent oc = new ObservationComponentComponent();
                setObservationValue(singleLabResult, oc);
                panelObservation.addComponent(oc);
            }
            res.add(panelObservation);
        }
        return res;
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
            return recordToObservation(labResult.get());
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

        // If filtering by code requested, collect the codes
        Set<String> wantedCodesSet = code == null ? Set.of()
                : code.getValuesAsQueryTokens().stream().map(c -> c.getValue()).collect(Collectors.toSet());
        // Gather panel codes, if any
        Set<String> panels = wantedCodesSet.stream().filter(c -> LOINCPanelsService.isKnown(c))
                .collect(Collectors.toSet());
        Set<String> NCGroups = wantedCodesSet.stream().filter(c -> NCGroupsService.isKnown(c))
                .collect(Collectors.toSet());
        Set<String> allPanelCodes = panels.stream().map(c -> LOINCPanelsService.getLOINCCodes(c)).flatMap(Set::stream)
                .collect(Collectors.toSet());
        Set<String> allNCCodes = NCGroups.stream().map(c -> NCGroupsService.getLOINCCodes(c)).flatMap(Set::stream)
                .collect(Collectors.toSet());
        log.debug("panels {} codes in panels {}, {} codes in NC {} groups ", panels.size(), allPanelCodes.size(),
                allNCCodes.size(), NCGroups.size());

        List<LabResultsRecord> labResult = repo.getLabResultsForPerson(pid);
        log.debug("lab results found {}", labResult.size());
        List<Observation> allRes = labResult.stream()
                .filter(o -> !(allPanelCodes.contains(o.loinc_code()) || allNCCodes.contains(o.loinc_code())))
                .map(o -> recordToObservation(o)).collect(Collectors.toList());
        List<Observation> res;
        if (code == null) {
            res = allRes;
            log.debug("no code filtering {}", res.size());
        } else {
            List<Observation> nonPanelRes = labResult.stream().filter(o -> wantedCodesSet.contains(o.loinc_code()))
                    .map(o -> recordToObservation(o)).collect(Collectors.toList());
            List<Observation> panelRes = panels.stream()
                    .map(p -> labResultsToPanels(labResult, p, LOINCPanelsService.getLOINCCodes(p), false))
                    .flatMap(List::stream).collect(Collectors.toList());
            List<Observation> NCGroupRes = NCGroups.stream()
                    .map(p -> labResultsToPanels(labResult, p, NCGroupsService.getLOINCCodes(p), true))
                    .flatMap(List::stream).collect(Collectors.toList());
            res = new LinkedList<>(nonPanelRes);
            res.addAll(panelRes);
            res.addAll(NCGroupRes);
            log.debug("filtered by code panel results {} and non-panel results {}", 
                panelRes.size() + NCGroupRes.size(),
                allRes.size());
        }

        // filter results in memory for now since this is just for one patient
        if (category != null && category.getValue() != null) {
            String catStr = category.getValue();
            Predicate<CodeableConcept> catMatch = c -> c.getCoding().stream()
                    .anyMatch(cd -> cd.hasCode() && catStr.equals(cd.getCode()));
            res = res.stream().filter(o -> o.getCategory().stream().anyMatch(catMatch)).collect(Collectors.toList());
            log.debug("after category filtering there are {} results", res.size());
        }
        return res;
    }

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }

}
