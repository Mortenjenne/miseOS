package app.dtos.gemini;

import java.util.List;

public record GeminiResponse(
    List<Candidate> candidates
)
{
}
