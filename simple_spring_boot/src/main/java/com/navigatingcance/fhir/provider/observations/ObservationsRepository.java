package com.navigatingcance.fhir.provider.observations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ObservationsRepository extends CrudRepository<LabResultsRecord, Integer>  {

    @Query("SELECT * FROM clinic_test_results where id = :oid")
    Optional<LabResultsRecord> getLabResultsById(@Param("oid") Integer oid);
    
    @Query("SELECT * FROM clinic_test_results where person_id = :pid")
    List<LabResultsRecord> getLabResultsForPerson(@Param("pid") Integer pid);
}
