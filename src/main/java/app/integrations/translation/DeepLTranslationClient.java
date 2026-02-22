package app.integrations.translation;

import app.dtos.translation.DeepLRequestDTO;
import app.dtos.translation.DeepLResponseDTO;
import app.dtos.translation.TranslationDTO;
import app.exceptions.TranslationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DeepLTranslationClient implements ITranslationClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;


    public DeepLTranslationClient(HttpClient client, ObjectMapper objectMapper, String apiUrl, String apiKey)
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
        catch (IOException | InterruptedException e)
        {
            throw new TranslationException("Translation network call failed: " + e.getMessage());
        }
        catch (Exception e)
        {
            throw new TranslationException("Translation error: " + e.getMessage());
        }
    }

    private String handleResponse(HttpResponse<String> response) throws Exception
    {
        if (response.statusCode() != 200)
        {
            throw new TranslationException("DeepL API error call (Status " + response.statusCode() + "): " + response.body());
        }

        DeepLResponseDTO responseDTO = objectMapper.readValue(response.body(), DeepLResponseDTO.class);

        if (responseDTO.translations() == null || responseDTO.translations().isEmpty())
        {
            throw new TranslationException("DeepL translation failed response was empty");
        }

        return responseDTO.translations().stream()
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
