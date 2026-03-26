package app.dtos.shopping;

import java.util.Map;

public record NormalizationResult(
    Map<String, String> normalizedNames,
    boolean normalized
)
{
}
