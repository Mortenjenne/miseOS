package app.dtos.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiDishSuggestionDTO(
    @JsonProperty("nameDA")
    String nameDA,

    @JsonProperty("descriptionDA")
    String descriptionDA
)
{
}
