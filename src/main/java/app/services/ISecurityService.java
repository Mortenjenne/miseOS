package app.services;

import app.dtos.security.AuthenticatedUser;
import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;

public interface ISecurityService
{
    LoginResponseDTO login(LoginRequestDTO dto);

    String createToken(Long userId, String email, String role);

    AuthenticatedUser verifyAndExtract(String token);
}
