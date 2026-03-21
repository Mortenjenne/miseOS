package app.testutils;

import app.dtos.security.LoginRequestDTO;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class TestAuthenticationUtil
{
    private TestAuthenticationUtil() {}

    private static String login(String email, String password)
    {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
            email,
            password
        );

        return given()
            .contentType(ContentType.JSON)
            .body(loginRequestDTO)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    public static String bearerToken(String email, String password)
    {
        return "Bearer " + login(email, password);
    }
}
