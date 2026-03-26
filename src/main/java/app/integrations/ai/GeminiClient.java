package app.integrations.ai;

import app.dtos.gemini.*;
import app.exceptions.AIIntegrationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GeminiClient implements IAiClient
{
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;
    private static final String MODEL_PRIMARY = "gemini-3.1-flash-lite-preview";
    private static final String MODEL_FALLBACK = "gemini-2.5-flash-lite";
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
            GeminiRequest geminiRequest = buildGeminiRequest(prompt);
            String jsonBody = objectMapper.writeValueAsString(geminiRequest);

            HttpResponse<String> response = sendRequestAndGetResponse(
                jsonBody,
                buildEndpoint(MODEL_PRIMARY, GEMINI_GENERATE_CONTENT),
                buildEndpoint(MODEL_FALLBACK, GEMINI_GENERATE_CONTENT)
            );

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
            String jsonBody = objectMapper.writeValueAsString(buildGeminiRequest(prompt));

            HttpRequest request = buildHttpRequest(
                jsonBody,
                buildEndpoint(MODEL_PRIMARY, GEMINI_STREAM_GENERATE_CONTENT)
            );

            client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .orTimeout(20, TimeUnit.SECONDS)
                .thenAccept(response ->
                {
                    try
                    {
                        if (response.statusCode() == 503 || (response.statusCode() == 429))
                        {
                            logger.warn("Primary model unavailable for stream — falling back to {}", MODEL_FALLBACK);
                            streamFromEndpoint(jsonBody, buildEndpoint(MODEL_FALLBACK, GEMINI_STREAM_GENERATE_CONTENT), chunkConsumer);
                        }
                        else
                        {
                            checkResponseCodes(response.statusCode(), GEMINI_STREAM_GENERATE_CONTENT);
                            readChunksFromStream(response.body(), chunkConsumer);
                        }
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

    private void streamFromEndpoint(String jsonBody, String endpoint, Consumer<String> chunkConsumer) throws IOException, InterruptedException
    {
        HttpRequest request = buildHttpRequest(jsonBody, endpoint);
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        checkResponseCodes(response.statusCode(), GEMINI_STREAM_GENERATE_CONTENT);
        readChunksFromStream(response.body(), chunkConsumer);
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

    private HttpResponse<String> sendRequestAndGetResponse(String jsonBody, String primaryEndpoint, String fallbackEndpoint) throws IOException, InterruptedException
    {
        System.out.println(primaryEndpoint);
        System.out.println(fallbackEndpoint);
        HttpRequest request = buildHttpRequest(jsonBody, primaryEndpoint);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 503 || (response.statusCode() == 429))
        {
            logger.warn("Primary model unavailable. Status code: ({}) — falling back to {}", response.statusCode(), MODEL_FALLBACK);
            request  = buildHttpRequest(jsonBody, fallbackEndpoint);
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        checkResponseCodes(response.statusCode(), GEMINI_GENERATE_CONTENT);

        return response;
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

    private void checkResponseCodes(int statusCode, String context)
    {
        if (statusCode == 503)
        {
            throw new AIIntegrationException("Gemini Service unavailable at the moment");
        }

        if (statusCode == 429)
        {
            throw new AIIntegrationException("Rate limit hit!");
        }

        if (statusCode != 200)
        {
            throw new AIIntegrationException("Gemini error on: " + context + " status code: " + statusCode);
        }
    }

    private String buildEndpoint(String model, String action)
    {
        return apiUrl + model + action;
    }
}
