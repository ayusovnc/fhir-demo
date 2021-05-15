package com.navigatingcance.fhir.provider.patient;

import java.util.List;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Address.AddressType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;

public record AddressRecord(
    String street_address_line1, String street_address_line2,
    String locality,
    String admin_district1, String admin_district2,
    String postal_code
) {
    public Address toAddress() {
        Address res = new Address();
        res.setUse(AddressUse.HOME);
        res.setType(AddressType.PHYSICAL);
        res.setCountry("USA");
        res.setPostalCode(postal_code);
        res.setCity(locality);
        StringType line1 = new StringType(street_address_line1);
        StringType line2 = new StringType(street_address_line2);
        res.setLine(List.of(line1, line2));
        return res;
    }

}
