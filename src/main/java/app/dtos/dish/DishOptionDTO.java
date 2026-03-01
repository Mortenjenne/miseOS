package app.dtos.dish;

public record DishOptionDTO(
    Long dishId,
    String dishName,
    String dishDescription,
    String stationName
)
{
}
