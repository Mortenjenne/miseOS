package app.integrations.translation;

import java.util.List;

public interface ITranslationClient
{
    List<String> translateBatch(List<String> texts, String targetLanguage);

    String translate(String text, String targetLanguage);
}
