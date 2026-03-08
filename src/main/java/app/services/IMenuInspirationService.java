package app.services;

import app.dtos.gemini.AiDishSuggestionDTO;

import java.util.List;

public interface IMenuInspirationService
{
    List<AiDishSuggestionDTO> getDailyInspiration(Long chefId);
}
