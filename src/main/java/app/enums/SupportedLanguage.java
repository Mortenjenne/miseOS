package app.enums;

import java.util.Arrays;

public enum SupportedLanguage
{
    EN("EN", "English"),
    ES("ES", "Spanish"),
    IT("IT", "Italian"),
    PT("PT", "Portuguese"),
    FR("FR", "French"),
    DE("DE", "German"),
    PL("PL", "Polish"),
    NL("NL", "Dutch");

    private final String code;
    private final String displayName;

    SupportedLanguage(String code, String displayName)
    {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() { return code; }

    public static SupportedLanguage fromCode(String code)
    {
        if (code == null || code.isBlank())
        {
            return EN;
        }

        return Arrays.stream(values())
            .filter(l -> l.code.equalsIgnoreCase(code.trim()))
            .findFirst()
            .orElse(EN);
    }
}
