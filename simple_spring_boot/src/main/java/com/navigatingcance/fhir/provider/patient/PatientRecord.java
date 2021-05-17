package com.navigatingcance.fhir.provider.patient;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;

public record PatientRecord(Integer id, String first_name, String last_name, String gender, Date birthdate,
        String race_codes, String ethnicity_key, String home_phone_number, String work_phone_number,
        String cell_phone_number, String preferred_language_code) {

    public Patient toPatient() {
        Patient res = new Patient();
        HumanName name = new HumanName();
        name.addGiven(first_name());
        name.setFamily(last_name());
        res.setName(List.of(name));
        if( gender != null ) {
            AdministrativeGender agender = switch(gender) {
                case "F" -> AdministrativeGender.FEMALE;
                case "M" -> AdministrativeGender.MALE;
                case "O" -> AdministrativeGender.OTHER;
                case "N" -> AdministrativeGender.UNKNOWN;
                case "T" -> AdministrativeGender.OTHER; // ?
                default -> AdministrativeGender.NULL;
            };
            res.setGender(agender);
        }
        res.setBirthDate(birthdate);

        // TODO. Race and etnicity not included in the base Patient FHIR resource

        // phones and email
        ContactPoint homePhone = new ContactPoint();
        homePhone.setSystem(ContactPointSystem.PHONE);
        homePhone.setValue(home_phone_number);
        homePhone.setUse(ContactPointUse.HOME);
        ContactPoint workPhone = new ContactPoint();
        workPhone.setSystem(ContactPointSystem.PHONE);
        workPhone.setValue(work_phone_number);
        workPhone.setUse(ContactPointUse.WORK);
        ContactPoint mobilePhone = new ContactPoint();
        mobilePhone.setSystem(ContactPointSystem.PHONE);
        mobilePhone.setValue(cell_phone_number);
        mobilePhone.setUse(ContactPointUse.MOBILE);
        // TODO. email goes here as well
        res.setTelecom(List.of(homePhone, workPhone, mobilePhone));

        return res;
    }

}
