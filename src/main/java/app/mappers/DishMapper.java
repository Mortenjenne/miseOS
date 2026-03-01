package app.mappers;

import app.dtos.dish.DishDTO;
import app.persistence.entities.Allergen;
import app.persistence.entities.Dish;

import java.util.stream.Collectors;

public class DishMapper
{
    private DishMapper() {}

    public static DishDTO toDTO(Dish dish)
    {
        return new DishDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getNameEN(),
            dish.getDescriptionDA(),
            dish.getDescriptionEN(),
            dish.getStation().getId(),
            dish.getStation().getStationName(),
            dish.getAllergens().stream()
                .map(Allergen::getName)
                .collect(Collectors.toSet()),
            dish.isActive(),
            dish.getOriginWeek(),
            dish.getOriginYear(),
            dish.getCreatedAt()
        );
    }
}
