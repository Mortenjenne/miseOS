package app.dtos.security;

public record TokenClaims(
    Long userId,
    String email,
    String role
)
{}
