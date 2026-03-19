package app.services;

import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;
import app.dtos.security.TokenClaims;

public interface ISecurityService
{
    LoginResponseDTO login(LoginRequestDTO dto);

    String createToken(Long userId, String email, String role);

    TokenClaims verifyAndExtract(String token);
}
