package app.services;

import app.dtos.DeepLRequestDTO;
import app.dtos.DeepLResponseDTO;
import app.dtos.TranslationDTO;
import app.exceptions.TranslationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class DeepLTranslationService implements ITranslationService
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;


    public DeepLTranslationService(HttpClient client, ObjectMapper objectMapper, String apiUrl, String apiKey)
    {
        this.client = client;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public String translate(String text, String language)
    {
        DeepLRequestDTO translationRequest = new DeepLRequestDTO(List.of(text), language);
        try
        {
            String jsonBody = objectMapper.writeValueAsString(translationRequest);
            HttpRequest request = buildHttpRequest(jsonBody);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return handleResponse(response);
        }
        catch (Exception e)
        {
            throw new TranslationException("Translation call failed " + e.getMessage());
        }
    }

    private String handleResponse(HttpResponse<String> response) throws Exception
    {
        if (response.statusCode() != 200)
        {
            throw new TranslationException("DeepL API error call (Status " + response.statusCode() + "): " + response.body());
        }

        DeepLResponseDTO responseDTO = objectMapper.readValue(response.body(), DeepLResponseDTO.class);

        if (responseDTO.translationDTOS() == null || responseDTO.translationDTOS().isEmpty())
        {
            throw new TranslationException("DeepL translation failed response was empty");
        }

        return responseDTO.translationDTOS().stream()
            .findFirst()
            .map(TranslationDTO::text)
            .orElseThrow(() -> new TranslationException("No translation found"));
    }

    private HttpRequest buildHttpRequest(String jsonBody)
    {
        return HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization" ,"DeepL-Auth-Key " + apiKey)
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    }
}
