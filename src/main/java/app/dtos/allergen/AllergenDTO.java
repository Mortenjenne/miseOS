package app.dtos.allergen;

public record AllergenDTO(
    Long id,
    String name,
    String description,
    Integer displayNumber
)
{
}
