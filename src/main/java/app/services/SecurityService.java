package app.services;

import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;
import app.dtos.security.AuthenticatedUser;
import app.enums.UserRole;
import app.exceptions.AuthenticationException;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

public class SecurityService implements ISecurityService
{
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    private final IUserReader userReader;
    private final String issuer;
    private final String secretKey;
    private final long expirationMs;

    public SecurityService(IUserReader userReader, String issuer, String secretKey, long expirationMs)
    {
        this.userReader = userReader;
        this.issuer = issuer;
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
        validateSecurityConfig();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto)
    {
        validateLoginRequest(dto);

        User user = userReader.findByEmail(dto.email())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!user.verifyPassword(dto.password()))
        {
            logger.warn("Login failed — wrong password for: {}", dto.email());
            throw new AuthenticationException("Invalid email or password");
        }

        String token = createToken(user.getId(), user.getEmail(), user.getUserRole().name());
        return toLoginResponseDTO(token, user);
    }

    @Override
    public String createToken(Long userId, String email, String role)
    {
        try
        {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issuer(issuer)
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role)
                .expirationTime(new Date(System.currentTimeMillis() + expirationMs))
                .issueTime(new Date())
                .build();

            JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256),
                new Payload(claims.toJSONObject())
            );

            jwsObject.sign(new MACSigner(secretKey));
            return jwsObject.serialize();
        }
        catch (JOSEException e)
        {
            throw new AuthenticationException("Could not create token");
        }
    }

    @Override
    public AuthenticatedUser verifyAndExtract(String token)
    {
        try
        {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new MACVerifier(secretKey)))
                throw new AuthenticationException("Token signature invalid");

            JWTClaimsSet claims = getJwtClaimsSet(jwt);

            Long userId = claims.getLongClaim("userId");
            String email = claims.getStringClaim("email");
            String role = claims.getStringClaim("role");

            if (userId == null || userId <= 0 || email == null || email.isBlank() || role == null || role.isBlank())
            {
                logger.warn("Token claims invalid — possible tampering");
                throw new AuthenticationException("Token claims invalid");
            }

            UserRole userRole = parseUserRoleClaim(role);

            return new AuthenticatedUser(userId, email, userRole);
        }
        catch (AuthenticationException e)
        {
            throw e;
        }
        catch (ParseException | JOSEException e)
        {
            throw new AuthenticationException("Token could not be verified");
        }
    }

    private JWTClaimsSet getJwtClaimsSet(SignedJWT jwt) throws ParseException
    {
        JWTClaimsSet claims = jwt.getJWTClaimsSet();

        Date now = new Date();

        if (claims.getExpirationTime() == null || claims.getExpirationTime().before(now))
        {
            throw new AuthenticationException("Token has expired");
        }

        if (claims.getNotBeforeTime() != null && now.before(claims.getNotBeforeTime()))
        {
            throw new AuthenticationException("Token not active yet");
        }

        if (!issuer.equals(claims.getIssuer()))
        {
            throw new AuthenticationException("Token issuer invalid");
        }
        return claims;
    }

    private UserRole parseUserRoleClaim(String roleClaim)
    {
        UserRole role;
        try
        {
            role = UserRole.valueOf(roleClaim.toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            throw new AuthenticationException("Token role invalid");
        }
        return role;
    }

    private void validateLoginRequest(LoginRequestDTO dto)
    {
        ValidationUtil.validateNotNull(dto, "Login request");
        ValidationUtil.validateNotBlank(dto.email(), "Email");
        ValidationUtil.validateNotBlank(dto.password(), "Password");
    }

    private LoginResponseDTO toLoginResponseDTO(String token, User user)
    {
        return new LoginResponseDTO(
            token,
            user.getEmail(),
            user.getUserRole().name());
    }

    private void validateSecurityConfig()
    {
        if (issuer == null || issuer.isBlank())
        {
            throw new IllegalStateException("JWT issuer must be configured");
        }

        if (secretKey == null || secretKey.isBlank())
        {
            throw new IllegalStateException("JWT secret key must be configured");
        }

        if (secretKey.getBytes().length < 32)
        {
            throw new IllegalStateException("JWT secret key too short. Use at least 32 bytes");
        }

        if (expirationMs <= 0)
        {
            throw new IllegalStateException("JWT expiration must be > 0");
        }
    }
}
