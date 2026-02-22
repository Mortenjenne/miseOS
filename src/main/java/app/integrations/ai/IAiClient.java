package app.integrations.ai;

import app.dtos.gemini.AiDishSuggestionDTO;

import java.util.List;
import java.util.Map;

public interface IAiClient
{
    public String generateResponse(String prompt);
}
