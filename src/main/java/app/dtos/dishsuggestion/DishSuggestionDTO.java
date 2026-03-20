package app.dtos.dishsuggestion;

import app.dtos.allergen.AllergenDTO;
import app.dtos.station.StationReferenceDTO;
import app.dtos.user.UserReferenceDTO;
import app.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DishSuggestionDTO(
    Long id,
    String nameDA,
    String descriptionDA,
    Status dishStatus,
    String feedback,
    StationReferenceDTO station,
    UserReferenceDTO createdBy,
    UserReferenceDTO reviewedBy,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime reviewedAt,
    List<AllergenDTO> allergens,
    Integer targetWeek,
    Integer targetYear,
    LocalDate deadline,
    boolean isPastDeadline,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime updatedAt
)
{}
