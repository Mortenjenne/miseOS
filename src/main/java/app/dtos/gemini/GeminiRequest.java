package app.dtos.gemini;

import java.util.List;

public record GeminiRequest(
    List<Content> contents,
    SystemInstruction systemInstruction,
    GenerationConfig generationConfig
    )
{
}
