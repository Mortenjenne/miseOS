package app.dtos.allergen;

public record AllergenCreateRequestDTO(
    String name,
    String description,
    Integer displayNumber,
    Long createdById
    )
{
}
