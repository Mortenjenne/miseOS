package app.dtos.station;

public record StationUpdateRequestDTO(
    Long stationId,
    String name,
    String description,
    Long editorId
)
{
}
