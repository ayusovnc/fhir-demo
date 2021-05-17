package com.navigatingcance.fhir.provider.observations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;

public record LabResultsRecord(Integer id, Integer clinic_id, Integer person_id, String external_id, String loinc_code,
        String quantity, String unit, String interpretation_concept, Date performed_on, String group_identifier,
        String component_name, String facility_component_name, Double normal_range_min, Double normal_range_max,
        Date reported_on, String lab_name, String lab_address, Integer sequence) {

    static Map<String, String> categoryMapping = new HashMap<String, String>() {{
        put("Vital Signs", "vital-signs");
        put("Hematology", "laboratory");
        put("CBC", "laboratory");
        put("Clinical Chemistry", "laboratory");
        put("CMP", "laboratory");
        put("General Chem", "value2");
        put("CBC w/ auto diff", "laboratory");
        put("Comprehensive metabolic panel","laboratory");
        put("CBC auto differential","laboratory");
    }};

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
        res.getValueQuantity().setValue(new BigDecimal(quantity));
        CodeableConcept cat = res.addCategory().setText(group_identifier);
        if (categoryMapping.containsKey(group_identifier)) {
            String observationCategory = categoryMapping.get(group_identifier);
            cat.addCoding().setSystem("http://hl7.org/fhir/observation-category").setCode(observationCategory);
            // TODO .setDisplay("Vital Signs");
        } else {
            res.addCategory().setText(group_identifier).addCoding();
        }
        return res;
    }

}
