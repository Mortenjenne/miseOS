package app.dtos.allergen;

public record AllergenUpdateRequestDTO(
    String name,
    String description,
    Integer displayNumber,
    Long editorId
)
{
}
