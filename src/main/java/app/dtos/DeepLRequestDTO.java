package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DeepLRequest(List<String> text,
                           @JsonProperty("target_lang")
                           String targetLanguage)
{
}
