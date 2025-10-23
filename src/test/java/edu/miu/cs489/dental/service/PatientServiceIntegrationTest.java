package edu.miu.cs489.dental.service;

import edu.miu.cs489.dental.model.Address;
import edu.miu.cs489.dental.model.Patient;
import edu.miu.cs489.dental.repository.AddressRepository;
import edu.miu.cs489.dental.repository.PatientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for PatientService.findPatientById()
 * These tests use the real database and Spring context
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PatientServiceIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AddressRepository addressRepository;


    @PersistenceContext
    private EntityManager entityManager;

    private Patient testPatient;
    private Address testAddress;

    @BeforeEach
    public void setUp() {
        // Clear persistence context to ensure clean state
        entityManager.clear();

        // Create test address
        testAddress = new Address();
        testAddress.setStreet("123 Test Street");
        testAddress.setCity("Test City");
        testAddress.setZipCode("12345");
        testAddress = addressRepository.save(testAddress);

        // Create test patient
        testPatient = new Patient();
        testPatient.setPatNo("P999");
        testPatient.setName("John Doe");
        testPatient.setAddress(testAddress);
        testPatient = patientRepository.save(testPatient);

        entityManager.flush();
    }

    /**
     * Test Case 1: Find patient by valid ID (patient exists)
     * Expected: Should return an Optional containing the patient
     */
    @Test
    public void testFindPatientById_WhenPatientExists_ShouldReturnPatient() {
        // Given: A patient exists in the database (created in setUp)
        Long existingPatientId = testPatient.getId();

        // When: We search for the patient by ID
        Optional<Patient> result = patientService.getPatientById(existingPatientId);

        // Then: The patient should be found
        assertTrue(result.isPresent(), "Patient should be found");
        assertEquals(existingPatientId, result.get().getId(), "Patient ID should match");
        assertEquals("P999", result.get().getPatNo(), "Patient number should match");
        assertEquals("John Doe", result.get().getName(), "Patient name should match");
        assertNotNull(result.get().getAddress(), "Patient should have an address");
        assertEquals("Test City", result.get().getAddress().getCity(), "Address city should match");
    }

    /**
     * Test Case 2: Find patient by invalid ID (patient does not exist)
     * Expected: Should return an empty Optional
     */
    @Test
    public void testFindPatientById_WhenPatientDoesNotExist_ShouldReturnEmpty() {
        // Given: An invalid patient ID that doesn't exist in the database
        Long invalidPatientId = 99999L;

        // When: We search for the patient by invalid ID
        Optional<Patient> result = patientService.getPatientById(invalidPatientId);

        // Then: The result should be empty
        assertFalse(result.isPresent(), "Patient should not be found");
        assertTrue(result.isEmpty(), "Optional should be empty");
    }

    /**
     * Additional Test: Verify patient data integrity after retrieval
     */
    @Test
    public void testFindPatientById_VerifyDataIntegrity() {
        // Given: A patient with complete information
        Long patientId = testPatient.getId();

        // When: We retrieve the patient
        Optional<Patient> result = patientService.getPatientById(patientId);

        // Then: All data should be intact
        assertTrue(result.isPresent());
        Patient retrievedPatient = result.get();

        assertNotNull(retrievedPatient.getId(), "Patient ID should not be null");
        assertNotNull(retrievedPatient.getPatNo(), "Patient number should not be null");
        assertNotNull(retrievedPatient.getName(), "Patient name should not be null");
        assertNotNull(retrievedPatient.getAddress(), "Patient address should not be null");

        // Verify address details
        assertEquals(testAddress.getId(), retrievedPatient.getAddress().getId(),
                "Address ID should match");
        assertEquals("123 Test Street", retrievedPatient.getAddress().getStreet(),
                "Address street should match");
    }
}

