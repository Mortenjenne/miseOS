package app.utils;

import java.util.List;

public class NormalizeTextPromptBuilder
{
    public static String buildNormalizeTextPrompt(String ingredientsJson, String languageName) throws Exception
    {
        return String.format(
            """
            Normalize these ingredient names to standard %s culinary terminology.

            Return ONLY valid JSON in this exact format:
            {"ingredient1": "Normalized1", "ingredient2": "Normalized2"}

            Rules:
            - Singular form
            - Capitalize first letter
            - Fix spelling
            - Translate to %s
            - NO markdown, NO explanation

            Ingredients: %s

            JSON:""",
            languageName,
            languageName,
            ingredientsJson
        );
    }
}
