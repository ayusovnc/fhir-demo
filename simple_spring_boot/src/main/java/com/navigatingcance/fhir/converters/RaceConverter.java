package com.navigatingcance.fhir.converters;

import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.codesystems.V3Race;
import org.hl7.fhir.r4.model.codesystems.V3RaceEnumFactory;

public class RaceConverter {
    // In GC race_codes seems to follow CDC Race and Ethnicity Code Set -Version 1.0
    // See:
    // https://www.health.ny.gov/statistics/sparcs/sysdoc/apprr.htm
    // https://www.hl7.org/fhir/v3/Race/cs.html
    public static Enumeration<V3Race> raceFromCDCCodeSetV1(String race) {
        String code = switch(race.toLowerCase()) {
            case "r1" -> "1002-5"; // AMERICAN INDIAN OR ALASKA NATIVE
            case "r2" -> "2028-9"; // ASIAN
            case "r3" -> "2054-5"; // BLACK OR AFRICAN-AMERICAN
            case "r4" -> "2076-8"; // NATIVE HAWAIIAN OR PACIFIC ISLANDER
            case "r5" -> "2106-3"; // WHITE
            case "r9" -> "2131-1"; // OTHER
            case "99" -> null;     // TODO. What is code 99?
            default -> null;
        };
        V3RaceEnumFactory raceEnumFact = new V3RaceEnumFactory();
        Enumeration<V3Race> re = new Enumeration<>(raceEnumFact, code);
        return re;
    }
}
