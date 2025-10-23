package edu.miu.cs489.dental.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    // Try multiple model endpoints - will use first one that works
    // Prioritizing Gemini 2.5 models with v1 API (stable)
    private static final String[] GEMINI_MODEL_ENDPOINTS = {
        "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash-exp:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash-latest:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro-latest:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent"
    };

    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 1000;
    private String workingEndpoint = null; // Cache the working endpoint

    private final OkHttpClient httpClient;
    private final Gson gson;

    public GeminiAIService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public String processWithAI(String userMessage, String context) {
        if (apiKey == null || apiKey.isEmpty()) {
            return null; // Fall back to rule-based processing
        }

        // Try each model endpoint until one works
        String[] endpointsToTry = workingEndpoint != null ?
            new String[]{workingEndpoint} : GEMINI_MODEL_ENDPOINTS;

        for (String endpoint : endpointsToTry) {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    String prompt = buildPrompt(userMessage, context);
                    String response = callGeminiAPI(prompt, attempt, endpoint);
                    String extracted = extractResponse(response);

                    if (extracted != null && !extracted.isEmpty()) {
                        // Cache this endpoint as working
                        if (workingEndpoint == null) {
                            workingEndpoint = endpoint;
                            System.out.println("✅ Found working Gemini endpoint: " + getModelName(endpoint));
                        }
                        return extracted;
                    }

                    if (attempt < MAX_RETRIES) {
                        System.out.println("Attempt " + attempt + " returned empty response, retrying...");
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Retry interrupted: " + e.getMessage());
                    return null;
                } catch (IOException e) {
                    if (e.getMessage().contains("404")) {
                        System.err.println("❌ Endpoint " + getModelName(endpoint) + " not available (404)");
                        break; // Try next endpoint
                    }

                    System.err.println("Error calling Gemini API with " + getModelName(endpoint) +
                        " (attempt " + attempt + "/" + MAX_RETRIES + "): " + e.getMessage());

                    if (attempt < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * attempt);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Unexpected error with " + getModelName(endpoint) + ": " + e.getMessage());
                    break; // Try next endpoint
                }
            }
        }

        System.err.println("⚠️ All Gemini endpoints failed, falling back to rule-based processing");
        return null; // Fall back to rule-based processing
    }

    private String getModelName(String endpoint) {
        if (endpoint.contains("gemini-2.5-flash")) return "gemini-2.5-flash";
        if (endpoint.contains("gemini-2.5-pro")) return "gemini-2.5-pro";
        if (endpoint.contains("gemini-2.0-flash-exp")) return "gemini-2.0-flash-exp";
        if (endpoint.contains("gemini-exp-1206")) return "gemini-exp-1206";
        if (endpoint.contains("gemini-1.5-flash-latest")) return "gemini-1.5-flash-latest";
        if (endpoint.contains("gemini-1.5-pro-latest")) return "gemini-1.5-pro-latest";
        if (endpoint.contains("gemini-1.5-flash")) return "gemini-1.5-flash";
        if (endpoint.contains("gemini-1.5-pro")) return "gemini-1.5-pro";
        if (endpoint.contains("gemini-pro")) return "gemini-pro";
        return "unknown";
    }

    private String buildPrompt(String userMessage, String context) {
        return String.format("""
            You are a helpful dental clinic assistant chatbot. Your role is to help users with:
            1. Finding patients and dentists
            2. Viewing appointments
            3. Creating appointments
            
            Context about the system:
            %s
            
            User message: %s
            
            Based on the user's message, provide a helpful response. If the user wants to:
            - Search for a patient/dentist: Extract the search term
            - Make an appointment: Extract patient name/ID, dentist name/ID, and preferred date/time
            - List data: Indicate what they want to see
            
            IMPORTANT: Respond ONLY with valid JSON. No markdown, no code blocks, no extra text.
            
            Use this exact format:
            {"intent":"search_patient","extracted_data":{"search_term":"John","patient_info":"","dentist_info":"","datetime":""},"response_message":"Looking for patient John"}
            
            Valid intent values: search_patient, search_dentist, list_patients, list_dentists, list_appointments, make_appointment, help, unknown
            
            For datetime, use ISO format like: 2025-10-25T14:00:00
            If a field is not applicable, use empty string "".
            
            Your JSON response:
            """, context, userMessage);
    }

    private String callGeminiAPI(String prompt, int attempt, String endpoint) throws IOException {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Add generation config for JSON response with stricter settings
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.3); // Lower temperature for more consistent output
        generationConfig.addProperty("topK", 20);
        generationConfig.addProperty("topP", 0.8);
        generationConfig.addProperty("maxOutputTokens", 512);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(endpoint + "?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Retry on 503 Service Unavailable
                if (response.code() == 503 && attempt < MAX_RETRIES) {
                    System.out.println("Gemini API returned 503, will retry (attempt " + attempt + "/" + MAX_RETRIES + ")");
                }
                throw new IOException("Gemini API call failed: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    private String extractResponse(String apiResponse) {
        try {
            JsonObject jsonResponse = gson.fromJson(apiResponse, JsonObject.class);
            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");

            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");

                if (parts != null && parts.size() > 0) {
                    JsonObject part = parts.get(0).getAsJsonObject();
                    String text = part.get("text").getAsString().trim();

                    // Clean the response - remove markdown code blocks if present
                    text = cleanJsonResponse(text);

                    // Validate it's valid JSON before returning
                    try {
                        JsonObject testParse = gson.fromJson(text, JsonObject.class);

                        // Ensure required fields exist
                        if (!testParse.has("intent")) {
                            System.err.println("AI response missing 'intent' field");
                            return null;
                        }

                        // Ensure extracted_data has all required fields
                        if (testParse.has("extracted_data")) {
                            JsonObject extractedData = testParse.getAsJsonObject("extracted_data");
                            if (!extractedData.has("search_term")) {
                                extractedData.addProperty("search_term", "");
                            }
                            if (!extractedData.has("patient_info")) {
                                extractedData.addProperty("patient_info", "");
                            }
                            if (!extractedData.has("dentist_info")) {
                                extractedData.addProperty("dentist_info", "");
                            }
                            if (!extractedData.has("datetime")) {
                                extractedData.addProperty("datetime", "");
                            }
                        } else {
                            // Add empty extracted_data if missing
                            JsonObject extractedData = new JsonObject();
                            extractedData.addProperty("search_term", "");
                            extractedData.addProperty("patient_info", "");
                            extractedData.addProperty("dentist_info", "");
                            extractedData.addProperty("datetime", "");
                            testParse.add("extracted_data", extractedData);
                        }

                        // Return the cleaned/fixed JSON
                        return testParse.toString();

                    } catch (com.google.gson.JsonSyntaxException e) {
                        System.err.println("AI returned invalid JSON: " + e.getMessage());
                        System.err.println("Raw response: " + text);
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Gemini response: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String cleanJsonResponse(String text) {
        // Remove markdown code blocks
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }

        // Remove any leading/trailing whitespace
        text = text.trim();

        // Find the first { and last }
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');

        if (firstBrace >= 0 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1);
        }

        return text;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}

