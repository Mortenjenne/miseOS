package app.services;

import java.util.List;
import java.util.Map;

public interface IAiClient
{
    Map<String, String> normalizeIngredientList(List<String> ingredients, String targetLanguage);
}
