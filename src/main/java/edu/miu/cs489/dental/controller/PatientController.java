package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.AddressSimpleDto;
import edu.miu.cs489.dental.dto.PatientWithAddressDto;
import edu.miu.cs489.dental.exception.ResourceNotFoundException;
import edu.miu.cs489.dental.model.Patient;
import edu.miu.cs489.dental.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adsweb/api/v1")
@Tag(name = "Patients", description = "Patient management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Operation(summary = "Get all patients", description = "Retrieve a list of all patients with their addresses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of patients",
                    content = @Content(schema = @Schema(implementation = PatientWithAddressDto.class)))
    })
    @GetMapping("/patients")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<PatientWithAddressDto> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return patients.stream().map(p -> {
            AddressSimpleDto addr = null;
            if (p.getAddress() != null) {
                addr = new AddressSimpleDto(p.getAddress().getId(), p.getAddress().getStreet(), p.getAddress().getCity(), p.getAddress().getZipCode());
            }
            return new PatientWithAddressDto(p.getId(), p.getPatNo(), p.getName(), addr);
        }).collect(Collectors.toList());
    }

    @Operation(summary = "Get patient by ID", description = "Retrieve a specific patient by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved patient",
                    content = @Content(schema = @Schema(implementation = PatientWithAddressDto.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content)
    })
    @GetMapping("/patients/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<PatientWithAddressDto> getPatientById(
            @Parameter(description = "Patient ID", required = true) @PathVariable Long id) {
        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        AddressSimpleDto addr = null;
        if (patient.getAddress() != null) {
            addr = new AddressSimpleDto(patient.getAddress().getId(), patient.getAddress().getStreet(), patient.getAddress().getCity(), patient.getAddress().getZipCode());
        }
        PatientWithAddressDto dto = new PatientWithAddressDto(patient.getId(), patient.getPatNo(), patient.getName(), addr);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Create new patient", description = "Create a new patient record (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient successfully created",
                    content = @Content(schema = @Schema(implementation = PatientWithAddressDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @PostMapping("/patients")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<PatientWithAddressDto> createPatient(@RequestBody Patient patient) {
        Patient created = patientService.createPatient(patient);
        AddressSimpleDto addr = null;
        if (created.getAddress() != null) {
            addr = new AddressSimpleDto(created.getAddress().getId(), created.getAddress().getStreet(), created.getAddress().getCity(), created.getAddress().getZipCode());
        }
        PatientWithAddressDto dto = new PatientWithAddressDto(created.getId(), created.getPatNo(), created.getName(), addr);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Update patient", description = "Update an existing patient record (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient successfully updated",
                    content = @Content(schema = @Schema(implementation = PatientWithAddressDto.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @PutMapping("/patient/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<PatientWithAddressDto> updatePatient(
            @Parameter(description = "Patient ID", required = true) @PathVariable Long id,
            @RequestBody Patient patientDetails) {
        Patient updated = patientService.updatePatient(id, patientDetails);
        AddressSimpleDto addr = null;
        if (updated.getAddress() != null) {
            addr = new AddressSimpleDto(updated.getAddress().getId(), updated.getAddress().getStreet(), updated.getAddress().getCity(), updated.getAddress().getZipCode());
        }
        PatientWithAddressDto dto = new PatientWithAddressDto(updated.getId(), updated.getPatNo(), updated.getName(), addr);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete patient", description = "Delete a patient record (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Patient successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Patient not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @DeleteMapping("/patient/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID", required = true) @PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search patients", description = "Search patients by name or patient number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                    content = @Content(schema = @Schema(implementation = PatientWithAddressDto.class)))
    })
    @GetMapping("/patient/search/{searchString}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<PatientWithAddressDto> searchPatients(
            @Parameter(description = "Search string", required = true) @PathVariable String searchString) {
        List<Patient> results = patientService.searchPatients(searchString);
        return results.stream().map(p -> {
            AddressSimpleDto addr = null;
            if (p.getAddress() != null) {
                addr = new AddressSimpleDto(p.getAddress().getId(), p.getAddress().getStreet(), p.getAddress().getCity(), p.getAddress().getZipCode());
            }
            return new PatientWithAddressDto(p.getId(), p.getPatNo(), p.getName(), addr);
        }).collect(Collectors.toList());
    }
}

