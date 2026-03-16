package app.utils;

public class NormalizeTextPromptBuilder
{
    private NormalizeTextPromptBuilder(){}

    public static String buildNormalizeTextPrompt(String ingredientsJson, String languageName)
    {
        return String.format(
            """
            Normalize these ingredient names to standard %s culinary terminology.

            Return ONLY valid JSON in this exact format:
            {"ingredient1": "Normalized1", "ingredient2": "Normalized2"}

            Rules:
            - Singular form
            - Capitalize first letter
            - Fix spelling errors (e.g. "hvidløch" → "Hvidløg")
            - Translate to %s
            - Merge only exact synonyms and different languages for the same ingredient
            (e.g. "onions" and "løg" are the same, "garlic" and "hvidløg" are the same)
            - Do NOT merge culinary variants that differ in use
            (e.g. "nye kartofler" and "kartofler" are different,
                "cherrytomater" and "tomater" are different,
                "rødløg" and "løg" are different)
            - NO markdown, NO explanation

            Ingredients: %s

            JSON:""",
            languageName,
            languageName,
            ingredientsJson
        );
    }
}
