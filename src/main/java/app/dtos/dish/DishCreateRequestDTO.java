package app.dtos.dish;

import java.util.Set;

public record DishCreateRequestDTO(String nameDA, String descriptionDA, Long userCreatedById, Long stationId, Set<Long> allergenIds)
{}
