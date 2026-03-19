package app.services;

import app.dtos.security.LoginRequestDTO;
import app.dtos.security.LoginResponseDTO;
import app.dtos.security.TokenClaims;
import app.exceptions.AuthenticationException;
import app.persistence.daos.interfaces.readers.IUserReader;
import app.persistence.entities.User;
import app.utils.ValidationUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Date;

public class SecurityService implements ISecurityService
{
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
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto)
    {
        validateLoginRequest(dto);

        User user = userReader.findByEmail(dto.email())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!user.verifyPassword(dto.password()))
        {
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
    public User verifyAndGetUser(String token)
    {
        TokenClaims claims = verifyAndExtract(token);
        return userReader.getByID(claims.userId());
    }

    private TokenClaims verifyAndExtract(String token)
    {
        try
        {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(new MACVerifier(secretKey)))
                throw new AuthenticationException("Token signature invalid");

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            if (claims.getExpirationTime().before(new Date()))
                throw new AuthenticationException("Token has expired");

            return new TokenClaims(
                claims.getLongClaim("userId"),
                claims.getStringClaim("email"),
                claims.getStringClaim("role")
            );
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
}
