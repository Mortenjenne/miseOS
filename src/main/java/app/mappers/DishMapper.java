package app.mappers;

import app.dtos.allergen.AllergenDTO;
import app.dtos.dish.DishDTO;
import app.dtos.dish.DishOptionDTO;
import app.dtos.menu.MenuDishDTO;
import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.Dish;

import java.util.List;

public class DishMapper
{
    private DishMapper() {}

    public static DishDTO toDTO(Dish dish)
    {
        List<AllergenDTO> allergens = getDishAllergens(dish);

        UserReferenceDTO createdBy = UserMapper.toReferenceDTO(dish.getCreatedBy());
        StationReferenceDTO station = StationMapper.toReferenceDTO(dish.getStation());
        boolean hasTranslations = dish.hasTranslation();

        return new DishDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getNameEN(),
            dish.getDescriptionDA(),
            dish.getDescriptionEN(),
            station,
            createdBy,
            allergens,
            dish.isActive(),
            dish.getOriginWeek(),
            dish.getOriginYear(),
            hasTranslations,
            dish.getCreatedAt(),
            dish.getUpdatedAt()
        );
    }

    public static DishOptionDTO toOptionDTO(Dish dish)
    {
        if (dish == null) return null;

        return new DishOptionDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getDescriptionDA(),
            dish.getStation().getStationName(),
            dish.isActive()
        );
    }

    public static MenuDishDTO toDishMenuDTO(Dish dish)
    {
        if (dish == null) return null;

        List<AllergenDTO> allergens = getDishAllergens(dish);

        return new MenuDishDTO(
            dish.getId(),
            dish.getNameDA(),
            dish.getDescriptionDA(),
            dish.getNameEN(),
            dish.getDescriptionEN(),
            dish.hasTranslation(),
            allergens
        );
    }

    private static List<AllergenDTO> getDishAllergens(Dish dish)
    {
        return dish.getAllergens()
            .stream()
            .map(AllergenMapper::toDTO)
            .toList();
    }
}
