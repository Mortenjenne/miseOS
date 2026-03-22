package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.testutils.TestAuthenticationUtil;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest
{
    private static final String ENDPOINT_URL = "/users";
    private static final int TEST_PORT = 7779;
    private static EntityManagerFactory emf;
    private static Javalin app;

    private Map<String, IEntity> seeded;
    private String headChefToken;
    private String sousChefToken;
    private String lineChefToken;

    @BeforeAll
    static void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_PORT;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void setup()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();

        seeded = populator.getSeededData();
        headChefToken = TestAuthenticationUtil.bearerToken("gordon@kitchen.com", "Hash1");
        lineChefToken = TestAuthenticationUtil.bearerToken("claire@pastry.com", "Hash2");
        sousChefToken = TestAuthenticationUtil.bearerToken("marco@grill.com", "Hash3");
    }

    @AfterAll
    static void stopServer()
    {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void getById()
    {
    }

    @Test
    void getAll()
    {
    }

    @Test
    void create()
    {
    }

    @Test
    void update()
    {
    }

    @Test
    void delete()
    {
    }

    @Test
    void getMe()
    {
    }

    @Test
    void changeRole()
    {
    }

    @Test
    void changeEmail()
    {
    }

    @Test
    void changePassword()
    {
    }

    @Test
    void assignToStation()
    {
    }
}
