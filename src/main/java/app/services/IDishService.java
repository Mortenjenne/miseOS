package app.services;

import app.dtos.dish.*;

import java.util.Map;
import java.util.Set;

public interface IDishService
{
    DishDTO createDish(Long creatorId, DishCreateDTO dto);

    DishDTO updateDish(Long editorId, Long dishId, DishUpdateDTO dto);

    DishDTO getById(Long dishId);

    DishDTO getByIdWithAllergens(Long dishId);

    Set<DishDTO> getAllActive();

    Set<DishDTO> searchByName(String query);

    boolean deleteDish(Long dishId, Long userId);

    DishDTO deactivate(Long dishId, Long userId);

    DishDTO activate(Long dishId, Long userId);

    AvailableDishesDTO getAvailableDishesForMenu(int week, int year);

    Map<String, Set<DishOptionDTO>> getAllActiveDishesGrouped();
}
