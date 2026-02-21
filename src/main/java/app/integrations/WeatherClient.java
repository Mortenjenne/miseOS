package app.integrations;

import app.dtos.weather.WeatherForecastDTO;
import app.exceptions.WeatherIntegrationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

    public WeatherForecastDTO getWeatherForecast()
    {
        HttpRequest request = buildHttpRequest();
        try
        {
            HttpResponse<String> response = sendRequest(request);
            System.out.println(response.body());
            WeatherForecastDTO weatherForecastDTO = objectMapper.readValue(response.body(), WeatherForecastDTO.class);

            if(weatherForecastDTO == null)
            {
                throw new WeatherIntegrationException("Open Meteo returned a empty forecast");
            }
            return weatherForecastDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new WeatherIntegrationException("Could not connect to Open-Meteo service");
        }
    }

    private HttpResponse<String> sendRequest (HttpRequest request) throws IOException, InterruptedException
    {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
        {
            throw new WeatherIntegrationException("Open-Meteo API error call (Status " + response.statusCode() + "): " + response.body());
        }

        return response;
    }

    private HttpRequest buildHttpRequest()
    {
        return HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .GET()
            .header("Content-Type", "application/json")
            .build();
    }
}
