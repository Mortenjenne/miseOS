package app.dtos.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Part(
    @JsonProperty("text")
    String text
)
{}
