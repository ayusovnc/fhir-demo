package com.navigatingcance.fhir.provider.observations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;

public record LabResultsRecord(Integer id, Integer clinic_id, Integer person_id, String external_id, String loinc_code,
        String quantity, String unit, String interpretation_concept, Date performed_on, String group_identifier,
        String component_name, String facility_component_name, Double normal_range_min, Double normal_range_max,
        Date reported_on, String lab_name, String lab_address, Integer sequence) {

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
        res.getValueQuantity().setValue(new BigDecimal(quantity));
        CodeableConcept group = res.addCategory().setText(group_identifier);
        ObservationCategory obsCat = groupNameToObsCategory(group_identifier);
        if( obsCat != ObservationCategory.NULL) {
            Coding groupCoding = res.addCategory().setText(group_identifier).addCoding();
            groupCoding.setSystem(obsCat.getSystem());
            groupCoding.setCode(obsCat.toCode());
            group.getCoding().add(groupCoding);
        }
        return res;
    }

}
