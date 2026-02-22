package app.dtos.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherUnitsDTO(

    @JsonProperty("temperature_2m_max")
    String temperatureUnit,

    @JsonProperty("precipitation_sum")
    String precipitationUnit
)
{
}
