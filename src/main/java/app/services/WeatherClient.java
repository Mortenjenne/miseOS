package app.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class WeatherClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final String URL = "https://api.open-meteo.com/v1/forecast?latitude=55.6759&longitude=12.5655&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode&timezone=Europe/Copenhagen&forecast_days=7";


    public WeatherClient(HttpClient client, ObjectMapper objectMapper)
    {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    private HttpRequest buildRequest()
    {
        return HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .GET()
            .header("Content-Type", "application/json")
            .build();
    }
}
