package app.dtos.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsageMetaData(
    @JsonProperty("promptTokenCount")
    Integer promptTokenCount,

    @JsonProperty("candidatesTokenCount")
    Integer candidatesTokenCount,

    @JsonProperty("totalTokenCount")
    Integer totalTokenCount
) {}
