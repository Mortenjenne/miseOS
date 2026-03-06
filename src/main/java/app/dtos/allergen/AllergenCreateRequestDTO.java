package app.dtos.allergen;

public record AllergenCreateRequestDTO(
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    Integer displayNumber
    )
{
}
