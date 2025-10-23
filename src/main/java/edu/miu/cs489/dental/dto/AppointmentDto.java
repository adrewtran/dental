package edu.miu.cs489.dental.dto;

import java.time.LocalDateTime;

public record AppointmentDto(
        Long id,
        LocalDateTime appointmentDateTime,
        PatientDto patient,
        DentistSimpleDto dentist,
        SurgeryDto surgery
) {
}

