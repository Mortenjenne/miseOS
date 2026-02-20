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

public class GeminiClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s";
    private String finalApiUrl;

    public GeminiClient(HttpClient client, ObjectMapper objectMapper, String apiKey)
    {
        this.client = client;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.finalApiUrl = String.format(API_URL, apiKey);
    }

    public Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage)
    {
        String languageName = targetLanguage.equals("da") ? "Danish" : "English";
        SystemInstruction systemInstruction = getSystemInstructions(languageName);
        try
        {
            String userInput = objectMapper.writeValueAsString(ingredients);
            List<Content> contents = mapInputToContent(userInput);
            GeminiRequest geminiRequest = new GeminiRequest(contents, systemInstruction);
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
        }
    }

    private String deSerializeResponse(String responseBody) throws JsonProcessingException
    {
        GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);

        if (geminiResponse.candidates() == null || geminiResponse.candidates().isEmpty())
        {
            throw new AIIntegrationException("No response from Gemini");
        }

        Candidate candidate = geminiResponse.candidates().get(0);

        if (candidate.content() == null || candidate.content().parts().isEmpty())
        {
            throw new AIIntegrationException("Empty response from Gemini");
        }

        return candidate.content().parts().get(0).text();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException
    {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
        String url = String.format(finalApiUrl);
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    }

    private List<Content> mapInputToContent(String userInput)
    {
        return List.of(new Content(List.of(new Part(userInput))));
    }

    private SystemInstruction getSystemInstructions(String languageName)
    {
        String systemPrompt = String.format(
            "You are an ingredient normalizer. " +
                "Normalize ingredient names to standard %s culinary terminology.\n" +
                "Return ONLY valid JSON mapping original names to normalized names.\n" +
                "Format: {\"original1\": \"Normalized1\", \"original2\": \"Normalized2\"}\n" +
                "Rules:\n" +
                "- Use singular form\n" +
                "- Proper capitalization\n" +
                "- Fix spelling mistakes\n" +
                "- Translate to %s if needed\n" +
                "- NO markdown, NO explanation, ONLY JSON",
            languageName,
            languageName
        );
        return new SystemInstruction(List.of(new Part(systemPrompt)));
    }


}
