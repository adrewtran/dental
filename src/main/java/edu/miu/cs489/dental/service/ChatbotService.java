package edu.miu.cs489.dental.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.miu.cs489.dental.dto.*;
import edu.miu.cs489.dental.model.Appointment;
import edu.miu.cs489.dental.model.Dentist;
import edu.miu.cs489.dental.model.Patient;
import edu.miu.cs489.dental.model.Surgery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DentistService dentistService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private GeminiAIService geminiAIService;

    private final Gson gson = new Gson();

    public ChatResponseDto processMessage(String message) {
        // Try AI-enhanced processing first if Gemini is configured
        if (geminiAIService.isConfigured()) {
            ChatResponseDto aiResponse = processWithAI(message);
            if (aiResponse != null) {
                return aiResponse;
            }
        }

        // Fall back to rule-based processing
        return processWithRules(message);
    }

    private ChatResponseDto processWithAI(String message) {
        try {
            String context = buildSystemContext();
            String aiResponse = geminiAIService.processWithAI(message, context);

            if (aiResponse != null) {
                return parseAIResponse(aiResponse, message);
            }
        } catch (Exception e) {
            System.err.println("AI processing failed, falling back to rules: " + e.getMessage());
        }
        return null;
    }

    private String buildSystemContext() {
        long patientCount = patientService.getAllPatients().size();
        long dentistCount = dentistService.getAllDentists().size();

        return String.format(
            "The dental system has %d patients and %d dentists registered. " +
            "Users can search for patients/dentists by name, view all records, or create appointments.",
            patientCount, dentistCount
        );
    }

    private ChatResponseDto parseAIResponse(String aiResponse, String originalMessage) {
        try {
            // Clean up the response - remove markdown code blocks if present
            String cleanResponse = aiResponse.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonObject jsonResponse = gson.fromJson(cleanResponse, JsonObject.class);
            String intent = jsonResponse.get("intent").getAsString();
            JsonObject extractedData = jsonResponse.has("extracted_data") ?
                jsonResponse.getAsJsonObject("extracted_data") : new JsonObject();
            String responseMessage = jsonResponse.has("response_message") ?
                jsonResponse.get("response_message").getAsString() : "";

            switch (intent) {
                case "search_patient":
                    String patientTerm = extractedData.has("search_term") ?
                        extractedData.get("search_term").getAsString() : "";
                    return searchPatient("find patient " + patientTerm);

                case "search_dentist":
                    String dentistTerm = extractedData.has("search_term") ?
                        extractedData.get("search_term").getAsString() : "";
                    return searchDentist("find dentist " + dentistTerm);

                case "make_appointment":
                    return handleAIAppointmentRequest(extractedData, responseMessage);

                case "list_patients":
                    return listAllPatients();

                case "list_dentists":
                    return listAllDentists();

                case "list_appointments":
                    return listAllAppointments();

                case "help":
                    return getHelpMessage();

                default:
                    return new ChatResponseDto(
                        responseMessage.isEmpty() ?
                            "I understand you want help, but I'm not sure exactly what you need. Try 'help' to see what I can do!" :
                            responseMessage,
                        "text",
                        null,
                        Arrays.asList("Help", "Find patient", "Find dentist", "Show appointments")
                    );
            }
        } catch (Exception e) {
            System.err.println("Failed to parse AI response: " + e.getMessage());
            return null;
        }
    }

    private ChatResponseDto handleAIAppointmentRequest(JsonObject extractedData, String aiMessage) {
        String patientInfo = extractedData.has("patient_info") ?
            extractedData.get("patient_info").getAsString() : "";
        String dentistInfo = extractedData.has("dentist_info") ?
            extractedData.get("dentist_info").getAsString() : "";
        String dateTime = extractedData.has("datetime") ?
            extractedData.get("datetime").getAsString() : "";

        // If we have all the information, try to create the appointment
        if (!patientInfo.isEmpty() && !dentistInfo.isEmpty() && !dateTime.isEmpty()) {
            return createAppointmentFromAI(patientInfo, dentistInfo, dateTime, aiMessage);
        }

        // Otherwise, provide guidance with what we know
        StringBuilder response = new StringBuilder("🤖 I can help you make an appointment! ");
        response.append(aiMessage.isEmpty() ? "" : aiMessage + "\n\n");

        if (patientInfo.isEmpty()) {
            response.append("\n👤 Please tell me the patient's name or ID.");
        }
        if (dentistInfo.isEmpty()) {
            response.append("\n👨‍⚕️ Please tell me which dentist you'd like to see.");
        }
        if (dateTime.isEmpty()) {
            response.append("\n📅 Please specify your preferred date and time (e.g., 'tomorrow at 2pm' or '2025-10-25 14:00').");
        }

        return new ChatResponseDto(
            response.toString(),
            "text",
            null,
            Arrays.asList("Find patient", "Find dentist", "List all appointments")
        );
    }

    private ChatResponseDto createAppointmentFromAI(String patientInfo, String dentistInfo,
                                                     String dateTime, String aiMessage) {
        try {
            // Find patient
            List<Patient> patients = patientService.searchPatients(patientInfo);
            if (patients.isEmpty()) {
                return new ChatResponseDto(
                    "❌ I couldn't find a patient matching '" + patientInfo + "'. Please search for the patient first.",
                    "text",
                    null,
                    Arrays.asList("Find patient " + patientInfo, "List all patients")
                );
            }
            Patient patient = patients.get(0);

            // Find dentist
            List<Dentist> dentists = dentistService.searchDentists(dentistInfo);
            if (dentists.isEmpty()) {
                return new ChatResponseDto(
                    "❌ I couldn't find a dentist matching '" + dentistInfo + "'. Please search for the dentist first.",
                    "text",
                    null,
                    Arrays.asList("Find dentist " + dentistInfo, "List all dentists")
                );
            }
            Dentist dentist = dentists.get(0);

            // Parse date/time
            LocalDateTime appointmentDateTime = parseDateTime(dateTime);
            if (appointmentDateTime == null) {
                return new ChatResponseDto(
                    "❌ I couldn't understand the date/time '" + dateTime + "'. Please use format like '2025-10-25 14:00' or 'tomorrow at 2pm'.",
                    "text",
                    null,
                    Arrays.asList("Try again with different time", "Show appointments")
                );
            }

            // Create the appointment
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDentist(dentist);
            appointment.setAppointmentDateTime(appointmentDateTime);

            Appointment created = appointmentService.createAppointment(appointment);

            // Create clean DTOs to avoid circular references
            PatientDto patientDto = new PatientDto(patient.getId(), patient.getPatNo(), patient.getName());
            DentistSimpleDto dentistDto = new DentistSimpleDto(dentist.getId(), dentist.getDentistName());

            SurgeryDto surgeryDto = null;
            if (created.getSurgery() != null) {
                AddressSimpleDto surgeryAddr = null;
                if (created.getSurgery().getAddress() != null) {
                    surgeryAddr = new AddressSimpleDto(
                        created.getSurgery().getAddress().getId(),
                        created.getSurgery().getAddress().getStreet(),
                        created.getSurgery().getAddress().getCity(),
                        created.getSurgery().getAddress().getZipCode()
                    );
                }
                surgeryDto = new SurgeryDto(created.getSurgery().getId(), created.getSurgery().getSurgeryNo(), surgeryAddr);
            }

            AppointmentDto appointmentDto = new AppointmentDto(
                created.getId(),
                created.getAppointmentDateTime(),
                patientDto,
                dentistDto,
                surgeryDto
            );

            return new ChatResponseDto(
                String.format("✅ Appointment created successfully!\n\n" +
                    "👤 Patient: %s\n" +
                    "👨‍⚕️ Dentist: %s\n" +
                    "📅 Date/Time: %s\n\n" +
                    "The appointment has been saved to the system.",
                    patient.getName(),
                    dentist.getDentistName(),
                    appointmentDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
                ),
                "appointment_created",
                appointmentDto,
                Arrays.asList("Show all appointments", "Make another appointment", "Find patient")
            );

        } catch (Exception e) {
            return new ChatResponseDto(
                "❌ Sorry, I encountered an error creating the appointment: " + e.getMessage(),
                "text",
                null,
                Arrays.asList("Try again", "Show appointments", "Help")
            );
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        // Try standard ISO format first
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Try common formats
            String[] formats = {
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd'T'HH:mm",
                "MM/dd/yyyy HH:mm",
                "dd/MM/yyyy HH:mm"
            };

            for (String format : formats) {
                try {
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(format));
                } catch (DateTimeParseException ex) {
                    // Continue to next format
                }
            }
        }
        return null;
    }

    private ChatResponseDto processWithRules(String message) {
        String lowerMessage = message.toLowerCase().trim();

        // Search for patients
        if (lowerMessage.contains("find patient") || lowerMessage.contains("search patient") ||
            lowerMessage.contains("patient named") || lowerMessage.contains("show patient")) {
            return searchPatient(message);
        }

        // Search for dentists
        if (lowerMessage.contains("find dentist") || lowerMessage.contains("search dentist") ||
            lowerMessage.contains("dentist named") || lowerMessage.contains("show dentist")) {
            return searchDentist(message);
        }

        // Make appointment
        if (lowerMessage.contains("make appointment") || lowerMessage.contains("book appointment") ||
            lowerMessage.contains("schedule appointment") || lowerMessage.contains("create appointment")) {
            return handleAppointmentRequest(message);
        }

        // List all patients
        if (lowerMessage.contains("list patients") || lowerMessage.contains("show all patients") ||
            lowerMessage.contains("all patients")) {
            return listAllPatients();
        }

        // List all dentists
        if (lowerMessage.contains("list dentists") || lowerMessage.contains("show all dentists") ||
            lowerMessage.contains("all dentists")) {
            return listAllDentists();
        }

        // Show appointments
        if (lowerMessage.contains("show appointments") || lowerMessage.contains("list appointments") ||
            lowerMessage.contains("all appointments") || lowerMessage.contains("upcoming appointments")) {
            return listAllAppointments();
        }

        // Help command
        if (lowerMessage.contains("help") || lowerMessage.equals("?")) {
            return getHelpMessage();
        }

        // Default response
        return getDefaultResponse();
    }

    private ChatResponseDto searchPatient(String message) {
        // Extract name from message
        String searchTerm = extractSearchTerm(message, Arrays.asList("find patient", "search patient", "patient named", "show patient"));

        if (searchTerm.isEmpty()) {
            return new ChatResponseDto(
                "Please provide a patient name to search. For example: 'Find patient John'",
                "text",
                null,
                Arrays.asList("List all patients", "Find dentist", "Make appointment")
            );
        }

        List<Patient> patients = patientService.searchPatients(searchTerm);

        if (patients.isEmpty()) {
            return new ChatResponseDto(
                "No patients found matching '" + searchTerm + "'. Would you like to see all patients?",
                "text",
                null,
                Arrays.asList("List all patients", "Try another search")
            );
        }

        List<PatientWithAddressDto> patientDtos = patients.stream().map(p -> {
            AddressSimpleDto addr = null;
            if (p.getAddress() != null) {
                addr = new AddressSimpleDto(p.getAddress().getId(),
                    p.getAddress().getStreet(),
                    p.getAddress().getCity(),
                    p.getAddress().getZipCode());
            }
            return new PatientWithAddressDto(p.getId(), p.getPatNo(), p.getName(), addr);
        }).collect(Collectors.toList());

        String responseMessage = "Found " + patients.size() + " patient(s) matching '" + searchTerm + "':";

        return new ChatResponseDto(
            responseMessage,
            "patient_list",
            patientDtos,
            Arrays.asList("Make appointment", "Find dentist", "Search another patient")
        );
    }

    private ChatResponseDto searchDentist(String message) {
        String searchTerm = extractSearchTerm(message, Arrays.asList("find dentist", "search dentist", "dentist named", "show dentist"));

        if (searchTerm.isEmpty()) {
            return new ChatResponseDto(
                "Please provide a dentist name to search. For example: 'Find dentist Smith'",
                "text",
                null,
                Arrays.asList("List all dentists", "Find patient", "Make appointment")
            );
        }

        List<Dentist> dentists = dentistService.searchDentists(searchTerm);

        if (dentists.isEmpty()) {
            return new ChatResponseDto(
                "No dentists found matching '" + searchTerm + "'. Would you like to see all dentists?",
                "text",
                null,
                Arrays.asList("List all dentists", "Try another search")
            );
        }

        List<DentistWithAddressDto> dentistDtos = dentists.stream().map(d -> {
            AddressSimpleDto addr = null;
            if (d.getAddress() != null) {
                addr = new AddressSimpleDto(d.getAddress().getId(),
                    d.getAddress().getStreet(),
                    d.getAddress().getCity(),
                    d.getAddress().getZipCode());
            }
            return new DentistWithAddressDto(d.getId(), d.getDentistName(), addr);
        }).collect(Collectors.toList());

        String responseMessage = "Found " + dentists.size() + " dentist(s) matching '" + searchTerm + "':";

        return new ChatResponseDto(
            responseMessage,
            "dentist_list",
            dentistDtos,
            Arrays.asList("Make appointment", "Find patient", "Search another dentist")
        );
    }

    private ChatResponseDto handleAppointmentRequest(String message) {
        return new ChatResponseDto(
            "To make an appointment, I need the following information:\n" +
            "1. Patient name or ID\n" +
            "2. Dentist name or ID\n" +
            "3. Preferred date and time\n\n" +
            "You can also use the 'Create Appointment' form from the navigation menu for a guided process.",
            "text",
            null,
            Arrays.asList("Find patient", "Find dentist", "List all appointments")
        );
    }

    private ChatResponseDto listAllPatients() {
        List<Patient> patients = patientService.getAllPatients();

        if (patients.isEmpty()) {
            return new ChatResponseDto(
                "No patients found in the system.",
                "text",
                null,
                Arrays.asList("Add new patient", "Show help")
            );
        }

        List<PatientWithAddressDto> patientDtos = patients.stream().map(p -> {
            AddressSimpleDto addr = null;
            if (p.getAddress() != null) {
                addr = new AddressSimpleDto(p.getAddress().getId(),
                    p.getAddress().getStreet(),
                    p.getAddress().getCity(),
                    p.getAddress().getZipCode());
            }
            return new PatientWithAddressDto(p.getId(), p.getPatNo(), p.getName(), addr);
        }).collect(Collectors.toList());

        return new ChatResponseDto(
            "Here are all patients in the system (" + patients.size() + " total):",
            "patient_list",
            patientDtos,
            Arrays.asList("Search patient", "Make appointment", "Find dentist")
        );
    }

    private ChatResponseDto listAllDentists() {
        List<Dentist> dentists = dentistService.getAllDentists();

        if (dentists.isEmpty()) {
            return new ChatResponseDto(
                "No dentists found in the system.",
                "text",
                null,
                Arrays.asList("Add new dentist", "Show help")
            );
        }

        List<DentistWithAddressDto> dentistDtos = dentists.stream().map(d -> {
            AddressSimpleDto addr = null;
            if (d.getAddress() != null) {
                addr = new AddressSimpleDto(d.getAddress().getId(),
                    d.getAddress().getStreet(),
                    d.getAddress().getCity(),
                    d.getAddress().getZipCode());
            }
            return new DentistWithAddressDto(d.getId(), d.getDentistName(), addr);
        }).collect(Collectors.toList());

        return new ChatResponseDto(
            "Here are all dentists in the system (" + dentists.size() + " total):",
            "dentist_list",
            dentistDtos,
            Arrays.asList("Search dentist", "Make appointment", "Find patient")
        );
    }

    private ChatResponseDto listAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();

        if (appointments.isEmpty()) {
            return new ChatResponseDto(
                "No appointments found in the system.",
                "text",
                null,
                Arrays.asList("Make appointment", "Find patient", "Find dentist")
            );
        }

        List<AppointmentDto> appointmentDtos = appointments.stream().map(a -> {
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
        }).collect(Collectors.toList());

        return new ChatResponseDto(
            "Here are all appointments (" + appointments.size() + " total):",
            "appointment_list",
            appointmentDtos,
            Arrays.asList("Make appointment", "Find patient", "Find dentist")
        );
    }

    private ChatResponseDto getHelpMessage() {
        String helpText = "🤖 Dental Assistant Bot Help\n\n" +
            "I can help you with:\n\n" +
            "📋 Patient Management:\n" +
            "  • 'Find patient [name]' - Search for a patient\n" +
            "  • 'List all patients' - Show all patients\n\n" +
            "👨‍⚕️ Dentist Management:\n" +
            "  • 'Find dentist [name]' - Search for a dentist\n" +
            "  • 'List all dentists' - Show all dentists\n\n" +
            "📅 Appointments:\n" +
            "  • 'Make appointment' - Get help creating an appointment\n" +
            "  • 'Show appointments' - View all appointments\n\n" +
            "Type 'help' anytime to see this message again!";

        return new ChatResponseDto(
            helpText,
            "text",
            null,
            Arrays.asList("Find patient", "Find dentist", "List all patients", "Show appointments")
        );
    }

    private ChatResponseDto getDefaultResponse() {
        return new ChatResponseDto(
            "I'm not sure I understand. I can help you search for patients, dentists, and manage appointments. " +
            "Type 'help' to see what I can do!",
            "text",
            null,
            Arrays.asList("Help", "Find patient", "Find dentist", "Show appointments")
        );
    }

    private String extractSearchTerm(String message, List<String> prefixes) {
        String lowerMessage = message.toLowerCase();
        for (String prefix : prefixes) {
            if (lowerMessage.contains(prefix)) {
                int startIndex = lowerMessage.indexOf(prefix) + prefix.length();
                String term = message.substring(startIndex).trim();
                // Remove common words
                term = term.replaceAll("(?i)^(for|with|the|a|an)\\s+", "");
                return term;
            }
        }
        return "";
    }
}

