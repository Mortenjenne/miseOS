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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplTest
{
    private static EntityManagerFactory emf;
    private static UserDAO userDAO;
    private static User user;

    @BeforeAll
    static void setup()
    {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
    }

    @BeforeEach
    void init()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.getTransaction().commit();
        }

        userDAO = new UserDAOImpl(emf);
        user = User.builder()
            .firstName("Mads")
            .lastName("Kok")
            .email("mads@miseos.dk")
            .hashedPassword("1234")
            .role(Role.LINE_COOK)
            .station(Station.HOT)
            .build();


    }

    @DisplayName("Test create and findByID")
    @Test
    void createAndFind()
    {
        User userFromDB = userDAO.create(user);

        assertTrue(userFromDB.getId() > 0);

        Optional<User> found = userDAO.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Mads", found.get().getFirstName());
        assertEquals("Kok", found.get().getLastName());
        assertTrue(found.get().getStation().equals(Station.HOT));
    }

    @DisplayName("Test find by email")
    @Test
    void findByEmail()
    {
        String email = user.getEmail();
        userDAO.create(user);
        Optional<User> found = userDAO.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @DisplayName("Test find by email, where email dosen't exit in db")
    @Test
    void findByEmailNotPresent()
    {
        String email = "test@notfound.dk";
        userDAO.create(user);
        Optional<User> found = userDAO.findByEmail(email);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find all users")
    void findAll()
    {
        userDAO.create(user);
        User user2 = User.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("benny@miseos.dk")
            .hashedPassword("5678")
            .role(Role.LINE_COOK)
            .station(Station.COLD)
            .build();
        userDAO.create(user2);

        List<User> users = userDAO.findAll();

        assertEquals(2, users.size());
        boolean hasMads = users.stream().anyMatch(u -> u.getFirstName().equals("Mads"));
        boolean hasBenny = users.stream().anyMatch(u -> u.getFirstName().equals("Jane"));

        assertTrue(hasMads);
        assertTrue(hasBenny);
    }

    @DisplayName("Test updating user")
    @Test
    void update()
    {
        User createdUser = userDAO.create(user);
        User userToUpdate = createdUser.toBuilder()
            .firstName("John")
            .lastName("Smith")
            .role(Role.CHEF_DE_PARTIE)
            .station(Station.COLD)
            .build();

        User updatedUser = userDAO.update(userToUpdate);

        assertEquals("John", updatedUser.getFirstName());
        assertEquals(Role.CHEF_DE_PARTIE, updatedUser.getRole());
        assertEquals(Station.COLD, updatedUser.getStation());
        assertEquals(createdUser.getId(), updatedUser.getId());

    }

    @Test
    @DisplayName("Test delete user")
    void delete()
    {
        userDAO.create(user);

        boolean deleted = userDAO.delete(user.getId());
        assertTrue(deleted);

        Optional<User> found = userDAO.findById(user.getId());
        assertFalse(found.isPresent());
    }

    @AfterAll
    static void tearDown()
    {
        if (emf != null) emf.close();
    }
}
