package app.dtos.translation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepLRequestDTO(
    @JsonProperty("text")
    List<String> text,

    @JsonProperty("target_lang")
    String targetLanguage)
{}
