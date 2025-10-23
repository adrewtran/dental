package edu.miu.cs489.dental.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiEndpointTester {

    private static final String API_KEY = "AIzaSyA9x3lQhHFxHjNqmPmXTnCEwOjEv0tp3cs";

    private static final String[] ENDPOINTS_TO_TEST = {
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent"
    };

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Gson gson = new Gson();

        System.out.println("Testing Gemini API Endpoints...\n");
        System.out.println("API Key: " + API_KEY.substring(0, 20) + "...\n");

        for (String endpoint : ENDPOINTS_TO_TEST) {
            System.out.println("Testing: " + getModelName(endpoint));

            try {
                JsonObject requestBody = new JsonObject();
                JsonArray contents = new JsonArray();
                JsonObject content = new JsonObject();
                JsonArray parts = new JsonArray();
                JsonObject part = new JsonObject();

                part.addProperty("text", "Say hello");
                parts.add(part);
                content.add("parts", parts);
                contents.add(content);
                requestBody.add("contents", contents);

                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                    .url(endpoint + "?key=" + API_KEY)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        JsonArray candidates = jsonResponse.getAsJsonArray("candidates");

                        if (candidates != null && candidates.size() > 0) {
                            System.out.println("✅ SUCCESS! Status: " + response.code());
                            System.out.println("   Response: " + candidates.get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts").get(0).getAsJsonObject()
                                .get("text").getAsString().substring(0, 50) + "...");
                            System.out.println("   ⭐ USE THIS ENDPOINT: " + endpoint);
                            System.out.println();
                            break; // Found working endpoint
                        }
                    } else {
                        System.out.println("❌ FAILED! Status: " + response.code() + " - " + response.message());
                    }
                } catch (Exception e) {
                    System.out.println("❌ ERROR: " + e.getMessage());
                }

            } catch (Exception e) {
                System.out.println("❌ ERROR: " + e.getMessage());
            }
            System.out.println();
        }

        System.out.println("\nTest completed.");
    }

    private static String getModelName(String endpoint) {
        if (endpoint.contains("gemini-pro")) return "gemini-pro";
        if (endpoint.contains("gemini-1.5-flash")) return "gemini-1.5-flash";
        if (endpoint.contains("gemini-1.5-pro")) return "gemini-1.5-pro";
        return "unknown";
    }
}

