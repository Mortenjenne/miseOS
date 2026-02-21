package app.dtos.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DailyWeatherDTO(
    @JsonProperty("time")
    List<String> days,

    @JsonProperty("temperature_2m_max")
    List<Double> maxTemperatures,

    @JsonProperty("temperature_2m_min")
    List<Double> minTemperatures,

    @JsonProperty("precipitation_sum")
    List<Double> precipitationSums,

    @JsonProperty("weathercode")
    List<Integer> weatherCodes
)
{}
