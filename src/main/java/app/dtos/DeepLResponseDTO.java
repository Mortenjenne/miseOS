package app.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepLResponseDTO(List<TranslationDTO> translationDTOS)
{
}
