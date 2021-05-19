package com.navigatingcance.fhir.provider.patient;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.navigatingcance.fhir.converters.GenderConverter;
import com.navigatingcance.fhir.converters.RaceConverter;

import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.codesystems.V3Race;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

public record PatientRecord(Integer id, String first_name, String last_name, String gender, Date birthdate,
        String race_codes, String ethnicity_key, String home_phone_number, String work_phone_number,
        String cell_phone_number, String preferred_language_code) {

    static Logger log = LoggerFactory.getLogger(PatientRecord.class);

    public Patient toPatient() {
        Patient res = new Patient();
        HumanName name = new HumanName();
        name.addGiven(first_name());
        name.setFamily(last_name());
        res.setName(List.of(name));
        if( gender != null ) {
            res.setGender(GenderConverter.genderFromGCCode(gender));
        }
        res.setBirthDate(birthdate);

        // Race and ethnicity not included in the base Patient FHIR resource,
        // race and ethnicity shows as extension
        if( race_codes != null ) {
            String[] codes = race_codes.split(",");
            for(String codeStr: codes) {
                Extension ext = new Extension();
                Enumeration<V3Race> code = RaceConverter.raceFromCDCCodeSetV1(codeStr.trim());
                if( code == null || code.getValue() == null ) {
                    log.warn("failed to decode race code {}", codeStr);
                    continue;
                }
                ext.setUrl(code.getSystem());
                ext.setValue(code);
                res.addExtension(ext);
            }
        }

        // phones and email
        List<ContactPoint> contactPoins = new LinkedList<>();
        if( home_phone_number != null ) {
            ContactPoint homePhone = new ContactPoint();
            homePhone.setSystem(ContactPointSystem.PHONE);
            homePhone.setValue(home_phone_number);
            homePhone.setUse(ContactPointUse.HOME);
            contactPoins.add(homePhone);
        }
        if( work_phone_number != null ) {
            ContactPoint workPhone = new ContactPoint();
            workPhone.setSystem(ContactPointSystem.PHONE);
            workPhone.setValue(work_phone_number);
            workPhone.setUse(ContactPointUse.WORK);
            contactPoins.add(workPhone);
        }
        if(cell_phone_number != null) {
            ContactPoint mobilePhone = new ContactPoint();
            mobilePhone.setSystem(ContactPointSystem.PHONE);
            mobilePhone.setValue(cell_phone_number);
            mobilePhone.setUse(ContactPointUse.MOBILE);
            contactPoins.add(mobilePhone);
        }
        // TODO. email goes here as well
        res.setTelecom(contactPoins);

        return res;
    }

}
