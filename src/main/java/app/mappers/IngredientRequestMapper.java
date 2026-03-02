package app.mappers;

import app.dtos.ingredient.IngredientRequestDTO;
import app.persistence.entities.IngredientRequest;

public class IngredientRequestMapper
{
    private IngredientRequestMapper() {}

    public static IngredientRequestDTO toDTO(IngredientRequest request)
    {
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
            request.getCreatedAt(),
            request.getReviewedAt(),
            request.getCreatedBy().getId(),
            request.getDish() != null ? request.getDish().getId() : null
        );
    }
}
