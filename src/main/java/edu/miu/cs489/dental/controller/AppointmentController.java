package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.*;
import edu.miu.cs489.dental.exception.ResourceNotFoundException;
import edu.miu.cs489.dental.model.Appointment;
import edu.miu.cs489.dental.service.AppointmentService;
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
@Tag(name = "Appointments", description = "Appointment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Operation(summary = "Get all appointments", description = "Retrieve a list of all appointments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of appointments",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class)))
    })
    @GetMapping("/appointments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<AppointmentDto> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return appointments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Operation(summary = "Get appointment by ID", description = "Retrieve a specific appointment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved appointment",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content)
    })
    @GetMapping("/appointments/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<AppointmentDto> getAppointmentById(
            @Parameter(description = "Appointment ID", required = true) @PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return ResponseEntity.ok(convertToDto(appointment));
    }

    @Operation(summary = "Create new appointment", description = "Create a new appointment (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment successfully created",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @PostMapping("/appointments")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<AppointmentDto> createAppointment(@RequestBody Appointment appointment) {
        Appointment created = appointmentService.createAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(created));
    }

    @Operation(summary = "Update appointment", description = "Update an existing appointment (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment successfully updated",
                    content = @Content(schema = @Schema(implementation = AppointmentDto.class))),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @PutMapping("/appointment/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<AppointmentDto> updateAppointment(
            @Parameter(description = "Appointment ID", required = true) @PathVariable Long id,
            @RequestBody Appointment appointmentDetails) {
        Appointment updated = appointmentService.updateAppointment(id, appointmentDetails);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @Operation(summary = "Delete appointment", description = "Delete an appointment (requires OFFICE_MANAGER role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires OFFICE_MANAGER role", content = @Content)
    })
    @DeleteMapping("/appointment/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<Void> deleteAppointment(
            @Parameter(description = "Appointment ID", required = true) @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    private AppointmentDto convertToDto(Appointment a) {
        PatientDto patientDto = null;
        if (a.getPatient() != null) {
            patientDto = new PatientDto(a.getPatient().getId(), a.getPatient().getPatNo(), a.getPatient().getName());
        }

        DentistSimpleDto dentistDto = null;
        if (a.getDentist() != null) {
            dentistDto = new DentistSimpleDto(a.getDentist().getId(), a.getDentist().getDentistName());
        }

        SurgeryDto surgeryDto = null;
        if (a.getSurgery() != null) {
            AddressSimpleDto surgeryAddr = null;
            if (a.getSurgery().getAddress() != null) {
                surgeryAddr = new AddressSimpleDto(a.getSurgery().getAddress().getId(),
                        a.getSurgery().getAddress().getStreet(),
                        a.getSurgery().getAddress().getCity(),
                        a.getSurgery().getAddress().getZipCode());
            }
            surgeryDto = new SurgeryDto(a.getSurgery().getId(), a.getSurgery().getSurgeryNo(), surgeryAddr);
        }

        return new AppointmentDto(a.getId(), a.getAppointmentDateTime(), patientDto, dentistDto, surgeryDto);
    }
}

