package edu.miu.cs489.dental.controller;

import edu.miu.cs489.dental.dto.*;
import edu.miu.cs489.dental.exception.ResourceNotFoundException;
import edu.miu.cs489.dental.model.Appointment;
import edu.miu.cs489.dental.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/adsweb/api/v1")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/appointments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public List<AppointmentDto> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return appointments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @GetMapping("/appointments/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_OFFICE_MANAGER')")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return ResponseEntity.ok(convertToDto(appointment));
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<AppointmentDto> createAppointment(@RequestBody Appointment appointment) {
        Appointment created = appointmentService.createAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(created));
    }

    @PutMapping("/appointment/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointmentDetails) {
        Appointment updated = appointmentService.updateAppointment(id, appointmentDetails);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @DeleteMapping("/appointment/{id}")
    @PreAuthorize("hasAuthority('ROLE_OFFICE_MANAGER')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
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

