package com.navigatingcance.fhir.provider.observations;

import java.util.Date;

public record LabResultsRecord(Integer id, Integer clinic_id, Integer person_id, String external_id, String loinc_code,
        String quantity, String unit, String interpretation_concept, Date performed_on, String group_identifier,
        String component_name, String facility_component_name, Double normal_range_min, Double normal_range_max,
        Date reported_on, String lab_name, String lab_address, Integer sequence) {

}
