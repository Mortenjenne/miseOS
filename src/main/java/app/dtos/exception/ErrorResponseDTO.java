package app.dtos.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
    int statusCode,
    String message,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime timestamp,
    String path,
    String referenceId
)
{
}
