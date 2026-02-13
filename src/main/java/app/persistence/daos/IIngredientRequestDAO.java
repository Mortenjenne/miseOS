package app.persistence.daos;

import app.enums.Status;
import app.persistence.entities.IngredientRequest;

import java.time.LocalDate;
import java.util.Set;

public interface IIngredientRequestDAO extends IEntityDAO<IngredientRequest, Long>
{
    Set<IngredientRequest> findByStatus(Status status);
    Set<IngredientRequest> findByStatusAndDeliveryDate(Status status, LocalDate deliveryDate);
}
