package app.dtos.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(
    @JsonProperty("candidates")
    List<Candidate> candidates,

    @JsonProperty("usageMetadata")
    UsageMetaData usageMetadata
)
{
}
