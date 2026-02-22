package app.dtos.translation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TranslationDTO(
    @JsonProperty("detected_source_language")
    String detectedSourceLanguage,

    @JsonProperty("text")
    String text
)
{}
