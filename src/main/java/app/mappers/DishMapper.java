package app.mappers;

import app.dtos.allergen.AllergenDTO;
import app.dtos.dish.DishDTO;
import app.dtos.dish.DishOptionDTO;
import app.persistence.entities.Allergen;
import app.persistence.entities.Dish;

import java.util.Set;
import java.util.stream.Collectors;

public class DishMapper
{
    private DishMapper() {}

    public static DishDTO toDTO(Dish dish)
    {
        Set<AllergenDTO> allergens = dish.getAllergens()
            .stream()
            .map(AllergenMapper::toDTO)
            .collect(Collectors.toSet());

        return new DishDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getNameEN(),
            dish.getDescriptionDA(),
            dish.getDescriptionEN(),
            dish.getStation().getId(),
            dish.getStation().getStationName(),
            allergens,
            dish.isActive(),
            dish.getOriginWeek(),
            dish.getOriginYear(),
            dish.getCreatedAt()
        );
    }

    public static DishOptionDTO toOptionDTO(Dish dish)
    {
        return new DishOptionDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getDescriptionDA(),
            dish.getStation().getStationName()
        );
    }
}
