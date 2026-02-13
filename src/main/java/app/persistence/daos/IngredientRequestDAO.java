package app.persistence.daos;

import app.enums.Status;
import app.persistence.entities.IngredientRequest;

import java.time.LocalDate;
import java.util.Set;

public class IngredientRequestDAO implements IIngredientRequestDAO
{
    @Override
    public Set<IngredientRequest> findByStatus(Status status)
    {
        return Set.of();
    }

    @Override
    public Set<IngredientRequest> findByStatusAndDeliveryDate(Status status, LocalDate deliveryDate)
    {
        return Set.of();
    }

    @Override
    public Set<IngredientRequest> findByDishSuggestionId(Long dishSuggestionId)
    {
        return Set.of();
    }

    @Override
    public Set<IngredientRequest> findByCreatedBy(Long userId)
    {
        return Set.of();
    }

    @Override
    public IngredientRequest create(IngredientRequest ingredientRequest)
    {
        return null;
    }

    @Override
    public Set<IngredientRequest> getAll()
    {
        return Set.of();
    }

    @Override
    public IngredientRequest getByID(Long id)
    {
        return null;
    }

    @Override
    public IngredientRequest update(IngredientRequest ingredientRequest)
    {
        return null;
    }

    @Override
    public boolean delete(Long id)
    {
        return false;
    }
}
