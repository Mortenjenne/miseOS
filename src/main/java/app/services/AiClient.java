package app.services;

import java.util.List;
import java.util.Map;

public interface AiClient
{
    Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage);
}
