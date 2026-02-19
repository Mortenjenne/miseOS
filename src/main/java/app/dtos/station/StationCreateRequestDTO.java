package app.dtos.station;

public record StationCreateRequestDTO(
    String name,
    String description,
    Long createdById
)
{
}
