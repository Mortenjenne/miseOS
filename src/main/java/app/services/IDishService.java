package app.services;

import app.dtos.dish.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDishService
{
    DishDTO createDish(Long creatorId, DishCreateDTO dto);

    DishDTO updateDish(Long editorId, Long dishId, DishUpdateDTO dto);

    DishDTO getById(Long dishId);

    List<DishDTO> searchByName(String query);

    boolean deleteDish(Long dishId, Long userId);

    DishDTO deactivate(Long dishId, Long userId);

    DishDTO activate(Long dishId, Long userId);

    AvailableDishesDTO getAvailableDishesForMenu(int week, int year);

    List<DishDTO> getAll(Long stationId, Boolean active);

    Map<String, List<DishOptionDTO>> getAllActiveDishesGrouped();
}
