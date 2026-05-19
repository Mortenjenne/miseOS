package app.dtos.station;

public record StationListDTO(
    Long id,
    String name,
    String description,
    Long userCount
) {}
