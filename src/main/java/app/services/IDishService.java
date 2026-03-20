package app.services;

import app.dtos.dish.*;
import app.dtos.security.AuthenticatedUser;

import java.util.List;
import java.util.Map;

public interface IDishService
{
    DishDTO createDish(AuthenticatedUser authUser, DishCreateDTO dto);

    DishDTO updateDish(Long dishId, DishUpdateDTO dto);

    DishDTO getById(Long dishId);

    List<DishDTO> searchByName(String query);

    boolean deleteDish(Long dishId);

    DishDTO deactivate(Long dishId);

    DishDTO activate(Long dishId);

    AvailableDishesDTO getAvailableDishesForMenu(int week, int year);

    List<DishDTO> getAll(Long stationId, Boolean active);

    Map<String, List<DishOptionDTO>> getAllActiveDishesGrouped();
}
