package app.services;

import app.dtos.gemini.*;
import app.exceptions.AIIntegrationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class GeminiClient implements IAiClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s";
    private final String finalApiUrl;

    public GeminiClient(HttpClient client, ObjectMapper objectMapper, String apiKey)
    {
        this.client = client;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.finalApiUrl = String.format(API_URL, apiKey);
    }

    @Override
    public Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage)
    {
        String languageName = targetLanguage.equals("da") ? "Danish" : "English";
        try
        {
            String prompt = buildPrompt(ingredients, targetLanguage);
            GeminiRequest geminiRequest = buildGeminiRequest(prompt);
            String geminiRequestBody = objectMapper.writeValueAsString(geminiRequest);
            HttpRequest request = buildHttpRequest(geminiRequestBody);
            HttpResponse<String> response = sendRequest(request);
            String geminiResponse = deSerializeResponse(response.body());
            String cleanedGeminiResponse = cleanGeminiResponse(geminiResponse);

            return objectMapper.readValue(cleanedGeminiResponse, new TypeReference<Map<String, String>>() {});
        }
        catch (IOException | InterruptedException e)
        {
            throw new AIIntegrationException("Could not connect to gemini service");
        } catch (Exception e)
        {
            throw new AIIntegrationException("Could not normalize list");
        }
    }

    private String deSerializeResponse(String responseBody) throws JsonProcessingException
    {
        System.out.println("Parsing response...");

        try {
            GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);

            if (geminiResponse.candidates() == null || geminiResponse.candidates().isEmpty()) {
                throw new AIIntegrationException("No candidates in Gemini response");
            }

            Candidate candidate = geminiResponse.candidates().get(0);

            if (candidate.content() == null || candidate.content().parts().isEmpty()) {
                throw new AIIntegrationException("Empty content in Gemini response");
            }

            // Check finish reason
            if (!"STOP".equals(candidate.finishReason())) {
                System.err.println("Warning: Finish reason was: " + candidate.finishReason());
            }

            String text = candidate.content().parts().get(0).text();

            System.out.println("Successfully extracted text from response");

            return text;

        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse JSON response!");
            System.err.println("Response was: " + responseBody);
            throw e;
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException
    {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        if (response.statusCode() == 429)
        {
            throw new AIIntegrationException("Rate limit hit!");
        }

        if (response.statusCode() != 200)
        {
            throw new AIIntegrationException("Gemini returned error code: " + response.statusCode());
        }
        return response;
    }

    private String cleanGeminiResponse(String geminiResponse)
    {
        return geminiResponse.replace("```json", "").replace("```", "").trim();
    }

    private HttpRequest buildHttpRequest(String jsonBody)
    {
        return HttpRequest.newBuilder()
            .uri(URI.create(finalApiUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    }

    private GeminiRequest buildGeminiRequest(String prompt)
    {
        return new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));
    }

    private String buildPrompt(List<String> ingredients, String languageName) throws Exception
    {
        String ingredientsJson = objectMapper.writeValueAsString(ingredients);

        return String.format(
            """
            Normalize these ingredient names to standard %s culinary terminology.

            Return ONLY valid JSON in this exact format:
            {"ingredient1": "Normalized1", "ingredient2": "Normalized2"}

            Rules:
            - Singular form
            - Capitalize first letter
            - Fix spelling
            - Translate to %s
            - NO markdown, NO explanation

            Ingredients: %s

            JSON:""",
            languageName,
            languageName,
            ingredientsJson
        );
    }


}
