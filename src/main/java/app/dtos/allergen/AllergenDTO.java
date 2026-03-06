package app.dtos.allergen;

public record AllergenDTO(
    Long id,
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    Integer displayNumber
)
{
}
