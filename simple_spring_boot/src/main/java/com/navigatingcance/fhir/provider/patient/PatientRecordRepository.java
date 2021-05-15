package com.navigatingcance.fhir.provider.patient;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

interface PatientRecordRepository extends CrudRepository<PatientRecord, Integer> {

    @Query("""
            SELECT * FROM patient_informations
            WHERE :age BETWEEN min_age and lmax_age
            """)
    List<PatientRecord> searchPatients(@Param("age") int age);

    @Query("SELECT * FROM patient_informations where id = :pid")
    Optional<PatientRecord> getPatientById(@Param("pid") Integer pid);

    @Query("SELECT * FROM addresses WHERE addressable_id = :pid")
    List<AddressRecord> getPatientAddresses(@Param("pid") Integer pid);
}
