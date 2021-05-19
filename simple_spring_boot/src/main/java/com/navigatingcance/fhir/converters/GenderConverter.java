package com.navigatingcance.fhir.converters;

import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;

public class GenderConverter {

    public static AdministrativeGender genderFromGCCode(String code) {
        AdministrativeGender agender = switch(code) {
            case "F" -> AdministrativeGender.FEMALE;
            case "M" -> AdministrativeGender.MALE;
            case "O" -> AdministrativeGender.OTHER;
            case "N" -> AdministrativeGender.UNKNOWN;
            case "T" -> AdministrativeGender.OTHER; // ?
            default -> AdministrativeGender.NULL;
        };
        return agender;
    }

}
