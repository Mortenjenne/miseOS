package app.daos.impl;

import app.config.HibernateConfig;
import app.daos.UserDAO;
import app.entities.User;
import app.enums.Role;
import app.enums.Station;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import javax.swing.text.html.parser.Entity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplTest
{
    private static EntityManagerFactory emf;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup()
    {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }

    @BeforeEach
    void init()
    {
        userDAO = new UserDAOImpl(emf);
    }

    @DisplayName("Test create and findByID")
    @Test
    void createAndFind()
    {
        User user = User.builder()
            .firstName("Mads")
            .lastName("Kok")
            .email("mads@miseos.dk")
            .hashedPassword("1234")
            .role(Role.LINE_COOK)
            .station(Station.HOT)
            .build();

        userDAO.create(user);

        assertTrue(user.getId() > 0);

        Optional<User> found = userDAO.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Mads", found.get().getFirstName());
    }

    @Test
    void findById()
    {
    }

    @Test
    void findByEmail()
    {
    }

    @Test
    void findAll()
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

    @AfterAll
    static void tearDown()
    {
        if (emf != null) emf.close();
    }
}
