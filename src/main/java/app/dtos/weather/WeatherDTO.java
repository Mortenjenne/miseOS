package app.dtos.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherDTO(
    @JsonProperty("time")
    LocalDate date,

    @JsonProperty("temperature_2m_max")
    double maxTemperature,

    @JsonProperty("temperature_2m_min")
    double minTemperature,

    @JsonProperty("precipitation_sum")
    double precipitationSumInMM,

    @JsonProperty("weathercode")
    int weatherCode
)
{}
