package edu.miu.cs489.dental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private String message;
    private String type; // "text", "patient_list", "dentist_list", "appointment_suggestion"
    private Object data; // Can contain list of patients, dentists, or appointment info
    private List<String> suggestions; // Quick action suggestions
}

