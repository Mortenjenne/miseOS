package app.dtos.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Candidate(
    @JsonProperty("content")
    Content content,

    @JsonProperty("finishReason")
    String finishReason,

    @JsonProperty("index")
    Integer index
)
{
}
