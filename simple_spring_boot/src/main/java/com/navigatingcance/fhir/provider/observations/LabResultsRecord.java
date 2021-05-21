package com.navigatingcance.fhir.provider.observations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LabResultsRecord(Integer id, Integer clinic_id, Integer person_id, String external_id, String loinc_code,
        String quantity, String unit, String interpretation_concept, Date performed_on, String group_identifier,
        String component_name, String facility_component_name, Double normal_range_min, Double normal_range_max,
        Date reported_on, String lab_name, String lab_address, Integer sequence) {

    static Logger log = LoggerFactory.getLogger(ObservationResourceProvider.class);

    public static ObservationCategory groupNameToObsCategory(String group_identifier) {
        if (group_identifier == null || group_identifier.isBlank()) {
            return ObservationCategory.NULL;
        }
        group_identifier = group_identifier.toLowerCase();
        if (group_identifier.contains("vital signs")) {
            return ObservationCategory.VITALSIGNS;
        }
        return ObservationCategory.LABORATORY;
    }

    public Observation toObservation() {
        Observation res = new Observation();
        res.setId(id.toString());
        res.setSubject(new Reference("Patient/" + person_id.toString())); // TODO. May be wrong. Person != Patient
        res.setStatus(ObservationStatus.FINAL);
        res.setEffective(new DateTimeType(performed_on));
        res.getCode().addCoding().setCode(loinc_code).setSystem("http://loinc.org");
        res.setValue(new Quantity());
        res.getValueQuantity().setCode(unit);
        res.getValueQuantity().setUnit(unit);
        try {
            res.getValueQuantity().setValue(new BigDecimal(quantity));
        } catch(Exception ex) {
            log.error("invalid quantity value {} in clinic_test_results {}", quantity, id); 
        }
        res.addCategory().setText(group_identifier);
        ObservationCategory obsCat = groupNameToObsCategory(group_identifier);
        if( obsCat != ObservationCategory.NULL) {
            Coding groupCoding = res.addCategory().setText(group_identifier).addCoding();
            groupCoding.setSystem(obsCat.getSystem());
            groupCoding.setCode(obsCat.toCode());
        }

        // reference range https://www.hl7.org/fhir/observation-definitions.html#Observation.referenceRange
        if (normal_range_min != null) {
            res.addReferenceRange().setLow(new Quantity(normal_range_min));
        }
        if (normal_range_max != null) {
            res.addReferenceRange().setHigh(new Quantity(normal_range_max));
        }

        // interpretation https://www.hl7.org/fhir/observation-definitions.html#Observation.interpretation
        CodeableConcept interpr = new CodeableConcept();
        interpr.setText(interpretation_concept);
        res.addInterpretation(interpr);
        // TODO. Add coding https://www.hl7.org/fhir/valueset-observation-category.html

        // Component https://www.hl7.org/fhir/observation-definitions.html#Observation.component
        res.addComponent().setValue(new StringType(component_name) );
        // TODO. Add component coding 

        return res;
    }

}
