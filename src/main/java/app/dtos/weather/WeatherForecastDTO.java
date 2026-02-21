package app.dtos.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherForecastDTO(

    @JsonProperty("timezone")
    String timeZone,

    @JsonProperty("daily_units")
    WeatherUnitsDTO weatherUnitsDTO,

    @JsonProperty("daily")
    List<WeatherDTO> weatherDTOS
)
{}
