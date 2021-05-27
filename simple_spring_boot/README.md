# Introduction

This is a POC implementation of the patient health records FHIR service.
The service is written in Java using Java 15 features.
The service is based on [reference Java FHIR server implementation](https://hapifhir.io/hapi-fhir/) . The service uses [Spring boot framework](https://spring.io/projects/spring-boot) for data access and to serve HTTP requests. 

The data is expected to be in GC DB compatible schema. GC DB itself can be used as well as Snowflake database.

# Running the service

## MySQL GC DB

For local MySQL DB installation the DB user name is expected to be `root` without any passwords. The DB name is expected to be `navcan_development`. Change the `applcation-dev.yaml` if that does not match your setup. Then you can run:

```
mvn spring-boot:run -Dspring-boot.run.profiles=sdev
```

## Snowflake 

Snowflake access path is set up in `application-snowdev.yaml`. The DB user name and password are to be provided in the env variables. 

```
export SF_USER=<db user name>
export SF_PASSWORD=<db user password>
mvn spring-boot:run -Dspring-boot.run.profiles=snowdev
```

# Performance testing

There are scripts checked in `perf_test` directory that can be used to test the service performance.

Scripts can be run concurrently with commands like this.
This example runs 2 clients concurrently, each one making 1000 calls.
The results of both runs are getting logged in the same log file.
```
for i in `seq 1 2`; do
    sh -c ./get_1000_random_patients.sh >> patient_get_1000_thr_2.tsv  &
done
```
Or like this.
This example runs 10 client concurrently, each making 100 calls.
The timing data of each run is logged in it's own file.
```
for i in `seq 1 10`; do
   sh -c ./get_100_random_patients.sh >> patient_get_100_thr_10_${i}.tsv &
done
```

# Examples

## Patient

```
curl 'http://localhost:8080/fhir/Patient/19'
```

## Lab results

```
curl 'http://localhost:8080/fhir/Observation?subject=597878'
curl 'http://localhost:8080/fhir/Observation?subject=19&category=laboratory'
```

```
curl 'http://localhost:8080/fhir/Observation?subject=19&code=26981-9'
```

## Blood pressure

```
curl 'http://localhost:8080/fhir/Observation?subject=19&category=vital-signs&code=8462-4,8480-6'
```

