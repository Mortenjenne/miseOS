package app.dtos.dish;

import app.dtos.allergen.AllergenDTO;
import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record DishDTO(
    Long id,
    String nameDA,
    String nameEN,
    String descriptionDA,
    String descriptionEN,
    StationReferenceDTO station,
    UserReferenceDTO createdBy,
    List<AllergenDTO> allergens,
    boolean active,
    int originWeek,
    int originYear,
    boolean hasTranslations,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime updatedAt
)
{}
