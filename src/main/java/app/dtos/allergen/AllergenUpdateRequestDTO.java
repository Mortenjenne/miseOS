package app.dtos.allergen;

public record AllergenUpdateRequestDTO(
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    Integer displayNumber
)
{
}
