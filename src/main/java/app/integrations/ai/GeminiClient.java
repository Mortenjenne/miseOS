package app.integrations.ai;

import app.dtos.gemini.*;
import app.exceptions.AIIntegrationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GeminiClient implements IAiClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;
    private static final String GEMINI_GENERATE_CONTENT = ":generateContent";
    private static final String GEMINI_STREAM_GENERATE_CONTENT = ":streamGenerateContent";

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
            String generateUrl = apiUrl + GEMINI_GENERATE_CONTENT;
            GeminiRequest geminiRequest = buildGeminiRequest(prompt);
            String jsonBody = objectMapper.writeValueAsString(geminiRequest);

            HttpRequest request = buildHttpRequest(jsonBody, generateUrl);
            HttpResponse<String> response = sendRequest(request);
            GeminiResponse geminiResponse = objectMapper.readValue(response.body(), GeminiResponse.class);
            String content = deSerializeResponse(geminiResponse);
            return cleanGeminiResponse(content);
        }
        catch (IOException | InterruptedException e)
        {
            throw new AIIntegrationException("Could not connect to Gemini service");
        }
    }

    @Override
    public void streamResponse(String prompt, Consumer<String> chunkConsumer, Consumer<Throwable> errorConsumer, Runnable onComplete)
    {
        try
        {
            String streamUrl = apiUrl + GEMINI_STREAM_GENERATE_CONTENT;
            String jsonBody = objectMapper.writeValueAsString(buildGeminiRequest(prompt));
            HttpRequest request = buildHttpRequest(jsonBody, streamUrl);

            client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .orTimeout(20, TimeUnit.SECONDS)
                .thenAccept(response ->
                {
                    try
                    {
                        checkResponseCodes(response);
                        readChunksFromStream(response.body(), chunkConsumer);
                    }
                    catch (Exception e)
                    {
                        errorConsumer.accept(new AIIntegrationException("Failed to read Gemini stream: " + e.getMessage()));
                    }
                    finally
                    {
                        onComplete.run();
                    }
                })
                .exceptionally(ex ->
                {
                    errorConsumer.accept(new AIIntegrationException("Gemini timed out or failed: " + ex.getMessage()));
                    onComplete.run();
                    return null;
                });
        }
        catch (JsonProcessingException e)
        {
            errorConsumer.accept(new AIIntegrationException("Could not serialize prompt: " + e.getMessage()));
        }
    }

    private void readChunksFromStream(InputStream inputStream, Consumer<String> chunkConsumer) throws IOException
    {
        JsonParser parser = objectMapper.getFactory().createParser(inputStream);

        if (parser.nextToken() != JsonToken.START_ARRAY) return;

        while (parser.nextToken() == JsonToken.START_OBJECT)
        {
            GeminiResponse chunk = objectMapper.readValue(parser, GeminiResponse.class);
            String text = deSerializeResponse(chunk);

            if (text != null && !text.isBlank())
            {
                chunkConsumer.accept(text);
            }
        }
        parser.close();
    }

    private String deSerializeResponse(GeminiResponse geminiResponse)
    {
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

    private HttpRequest buildHttpRequest(String jsonBody, String url)
    {
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    }

    private GeminiRequest buildGeminiRequest(String prompt)
    {
        return new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));
    }

    private void checkResponseCodes(HttpResponse<InputStream> response)
    {
        if (response.statusCode() == 429)
        {
            throw new AIIntegrationException("Rate limit hit!");
        }

        if (response.statusCode() != 200)
        {
            throw new AIIntegrationException("Gemini stream error: " + response.statusCode());
        }
    }
}
