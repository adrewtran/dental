package edu.miu.cs489.dental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAppointmentRequest {
    private String patientInfo; // Can be name or ID
    private String dentistInfo; // Can be name or ID
    private String dateTime; // Natural language or ISO format
    private Long surgeryId; // Optional
}

