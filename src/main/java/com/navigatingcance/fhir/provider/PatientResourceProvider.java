package com.navigatingcance.fhir.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
public class PatientResourceProvider implements IResourceProvider {

   /**
    * This map has a resource ID as a key, and each key maps to a Deque list containing all versions of the resource with that ID.
    */
   private Map<Long, Deque<Patient>> myIdToPatientVersions = new HashMap<Long, Deque<Patient>>();

   /**
    * This is used to generate new IDs
    */
   private long myNextId = 1;

   /**
    * Constructor, which pre-populates the provider with one resource instance.
    */
   public PatientResourceProvider() {
      long resourceId = myNextId++;

      Patient patient = new Patient();
      patient.setId(Long.toString(resourceId));
      patient.addIdentifier();
      patient.getIdentifier().get(0).setSystem("urn:hapitest:mrns");
      patient.getIdentifier().get(0).setValue("00002");
      patient.addName().setFamily("Test");
      patient.getName().get(0).addGiven("PatientOne");
      patient.setGender(AdministrativeGender.FEMALE);

      LinkedList<Patient> list = new LinkedList<>();
      list.add(patient);


      myIdToPatientVersions.put(resourceId, list);

   }

   /**
    * Stores a new version of the patient in memory so that it can be retrieved later.
    *
    * @param thePatient The patient resource to store
    * @param theId      The ID of the patient to retrieve
    */
   private void addNewVersion(Patient thePatient, Long theId) {

      log.info("addNewVersion called with Id {}", theId);

      if (!myIdToPatientVersions.containsKey(theId)) {
         myIdToPatientVersions.put(theId, new LinkedList<>());
      }

      thePatient.getMeta().setLastUpdatedElement(InstantType.withCurrentTime());

      Deque<Patient> existingVersions = myIdToPatientVersions.get(theId);

      // We just use the current number of versions as the next version number
      String newVersion = Integer.toString(existingVersions.size());

      // Create an ID with the new version and assign it back to the resource
      IdType newId = new IdType("Patient", Long.toString(theId), newVersion);
      thePatient.setId(newId);

      existingVersions.add(thePatient);
   }

   /**
    * The "@Create" annotation indicates that this method implements "create=type", which adds a
    * new instance of a resource to the server.
    */
   @Create()
   public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
      log.info("createPatient called with {}", thePatient);

      validateResource(thePatient);

      // Here we are just generating IDs sequentially
      long id = myNextId++;

      addNewVersion(thePatient, id);

      // Let the caller know the ID of the newly created resource
      return new MethodOutcome(new IdType(id));
   }

   /**
    * The "@Search" annotation indicates that this method supports the search operation. You may have many different method annotated with this annotation, to support many different search criteria.
    * This example searches by family name.
    *
    * @param theFamilyName This operation takes one parameter which is the search criteria. It is annotated with the "@Required" annotation. This annotation takes one argument, a string containing the name of
    *                      the search criteria. The datatype here is StringDt, but there are other possible parameter types depending on the specific search criteria.
    * @return This method returns a list of Patients. This list may contain multiple matching resources, or it may also be empty.
    */
   @Search()
   public List<Patient> findPatientsByName(@RequiredParam(name = Patient.SP_FAMILY) StringType theFamilyName) {

      log.info("findPatientsByName called with {}", theFamilyName);

      /*
       * Look for all patients matching the name
       */
      String theFamilyNameStr = theFamilyName.asStringValue();
      Predicate<Patient> doesNameMatch = p -> {
         return p.getName().stream().anyMatch(nm -> theFamilyNameStr.equals(nm.getFamily()));
      };

      List<Patient> retVal = new LinkedList<Patient>();
      retVal = myIdToPatientVersions.values().stream().map(p -> p.getLast()).filter(doesNameMatch)
            .collect(Collectors.toList());

      return retVal;
   }

   @Search
   public List<Patient> findPatientsUsingArbitraryCtriteria() {
      log.info("findPatientsUsingArbitraryCtriteria called");

      List<Patient> retVal = new LinkedList<Patient>();

      retVal = myIdToPatientVersions.values().stream().map(p -> p.getLast()).collect(Collectors.toList());

      return retVal;
   }

   /**
    * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
    */
   @Override
   public Class<Patient> getResourceType() {
      return Patient.class;
   }

   /**
    * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
    * <p>
    * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
    * </p>
    *
    * @param theId The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
    * @return Returns a resource matching this identifier, or null if none exists.
    */
   @Read(version = true)
   public Patient readPatient(@IdParam IdType theId) {

      log.info("readPatient called with {}", theId);      

      Deque<Patient> retVal;
      try {
         retVal = myIdToPatientVersions.get(theId.getIdPartAsLong());
      } catch (NumberFormatException e) {
         /*
          * If we can't parse the ID as a long, it's not valid so this is an unknown resource
          */
         throw new ResourceNotFoundException(theId);
      }

      if (theId.hasVersionIdPart() == false) {
         return retVal.getLast();
      } else {
         String searchVer = theId.getVersionIdPart();
         Optional<Patient> foundVer = 
            retVal.stream().filter(p -> searchVer.equals(p.getIdElement().getVersionIdPart())).findFirst();
         if (foundVer.isPresent()) {
            return foundVer.get();
         }
         // No matching version
         throw new ResourceNotFoundException("Unknown version: " + theId.getValue());
      }

   }

   /**
    * The "@Update" annotation indicates that this method supports replacing an existing
    * resource (by ID) with a new instance of that resource.
    *
    * @param theId      This is the ID of the patient to update
    * @param thePatient This is the actual resource to save
    * @return This method returns a "MethodOutcome"
    */
   @Update()
   public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient) {

      log.info("updatePatient called with {}", theId);      

      validateResource(thePatient);

      Long id;
      try {
         id = theId.getIdPartAsLong();
      } catch (DataFormatException e) {
         throw new InvalidRequestException("Invalid ID " + theId.getValue() + " - Must be numeric");
      }

      /*
       * Throw an exception (HTTP 404) if the ID is not known
       */
      if (!myIdToPatientVersions.containsKey(id)) {
         throw new ResourceNotFoundException(theId);
      }

      addNewVersion(thePatient, id);

      return new MethodOutcome();
   }

   /**
    * This method just provides simple business validation for resources we are storing.
    *
    * @param thePatient The patient to validate
    */
   private void validateResource(Patient thePatient) {
      /*
       * Our server will have a rule that patients must have a family name or we will reject them
       */
      if (thePatient.getNameFirstRep().getFamily().isEmpty()) {
         OperationOutcome outcome = new OperationOutcome();
         outcome.addIssue().setSeverity(IssueSeverity.FATAL).setDiagnostics("No family name provided, Patient resources must have at least one family name.");
         throw new UnprocessableEntityException(FhirContext.forDstu3(), outcome);
      }
   }
}
