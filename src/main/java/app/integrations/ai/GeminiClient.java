package app.integrations.ai;

import app.dtos.gemini.*;
import app.exceptions.AIIntegrationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GeminiClient implements IAiClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;

    public GeminiClient(HttpClient client, ObjectMapper objectMapper, String apiKey, String apiUrl)
    {
        this.client = client;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String generateResponse(String prompt)
    {
        try
        {
            GeminiRequest geminiRequest = buildGeminiRequest(prompt);
            String jsonBody = objectMapper.writeValueAsString(geminiRequest);

            HttpRequest request = buildHttpRequest(jsonBody);
            HttpResponse<String> response = sendRequest(request);

            String content = deSerializeResponse(response.body());
            return cleanGeminiResponse(content);
        }
        catch (IOException | InterruptedException e)
        {
            throw new AIIntegrationException("Could not connect to gemini service");
        }
    }

    private String deSerializeResponse(String responseBody) throws JsonProcessingException
    {
        GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);

        if (geminiResponse.candidates() == null || geminiResponse.candidates().isEmpty()) {
            throw new AIIntegrationException("No candidates in Gemini response");
        }

        Candidate candidate = geminiResponse.candidates().get(0);

        if (candidate.content() == null || candidate.content().parts().isEmpty()) {
            throw new AIIntegrationException("Empty content in Gemini response");
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
        return HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    }

    private GeminiRequest buildGeminiRequest(String prompt)
    {
        return new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));
    }
}
