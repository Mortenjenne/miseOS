package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepLRequestDTO(List<String> text,
                              @JsonProperty("target_lang")
                           String targetLanguage)
{
}
