package app.mappers;

import app.dtos.dish.DishReferenceDTO;
import app.dtos.ingredient.IngredientRequestDTO;
import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.persistence.entities.IngredientRequest;

public class IngredientRequestMapper
{
    private IngredientRequestMapper() {}

    public static IngredientRequestDTO toDTO(IngredientRequest request)
    {
        DishReferenceDTO dish = DishMapper.toDishReferenceDTO(request.getDish());
        UserReferenceDTO requestedBy = UserMapper.toReferenceDTO(request.getCreatedBy());
        UserReferenceDTO reviewedBy = UserMapper.toReferenceDTO(request.getReviewedBy());
        StationReferenceDTO station = StationMapper.toReferenceDTO(request.getCreatedBy().getStation());

        return new IngredientRequestDTO(
            request.getId(),
            request.getName(),
            request.getQuantity(),
            request.getUnit(),
            request.getPreferredSupplier(),
            request.getNote(),
            request.getRequestStatus(),
            request.getRequestType(),
            request.getDeliveryDate(),
            requestedBy,
            reviewedBy,
            dish,
            station,
            request.getReviewedAt(),
            request.getCreatedAt(),
            request.getUpdatedAt()
        );
    }
}
