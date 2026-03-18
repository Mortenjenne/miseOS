package app.persistence.daos.interfaces;

import app.enums.RequestType;
import app.enums.Status;
import app.persistence.daos.interfaces.readers.IIngredientRequestReader;
import app.persistence.entities.IngredientRequest;

import java.time.LocalDate;
import java.util.List;

public interface IIngredientRequestDAO extends IIngredientRequestReader
{
    List<IngredientRequest> findByFilter(Status status, LocalDate deliveryDate, Long creatorId, RequestType requestType, Long stationId);
}
