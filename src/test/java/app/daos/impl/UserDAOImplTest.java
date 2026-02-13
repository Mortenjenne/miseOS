package app.daos.impl;

import app.persistence.config.HibernateConfig;
import app.persistence.daos.IUserDAO;
import app.persistence.daos.impl.UserDAOImpl;
import app.persistence.entities.User;
import app.enums.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOImplTest
{
    private static EntityManagerFactory emf;
    private static IUserDAO IUserDAO;
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

        IUserDAO = new UserDAOImpl(emf);
        user = User.builder()
            .firstName("Mads")
            .lastName("Kok")
            .email("mads@miseos.dk")
            .hashedPassword("1234")
            .userRole(UserRole.LINE_COOK)
            .station(Station.HOT)
            .build();


    }

    @DisplayName("Test create and findByID")
    @Test
    void createAndFind()
    {
        User userFromDB = IUserDAO.create(user);

        assertTrue(userFromDB.getId() > 0);

        Optional<User> found = IUserDAO.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Mads", found.get().getFirstName());
        assertEquals("Kok", found.get().getLastName());
    }

    @DisplayName("Test find by email")
    @Test
    void findByEmail()
    {
        String email = user.getEmail();
        IUserDAO.create(user);
        Optional<User> found = IUserDAO.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @DisplayName("Test find by email, where email dosen't exit in db")
    @Test
    void findByEmailNotPresent()
    {
        String email = "test@notfound.dk";
        IUserDAO.create(user);
        Optional<User> found = IUserDAO.findByEmail(email);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Test find all users")
    void findAll()
    {
        IUserDAO.create(user);
        User user2 = User.builder()
            .firstName("Jane")
            .lastName("Doe")
            .email("benny@miseos.dk")
            .hashedPassword("5678")
            .userRole(UserRole.LINE_COOK)
            .station(Station.COLD)
            .build();
        IUserDAO.create(user2);

        List<User> users = IUserDAO.findAll();

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
        User createdUser = IUserDAO.create(user);
        User userToUpdate = createdUser.toBuilder()
            .firstName("John")
            .lastName("Smith")
            .userRole(UserRole.CHEF_DE_PARTIE)
            .station(Station.COLD)
            .build();

        User updatedUser = IUserDAO.update(userToUpdate);

        assertEquals("John", updatedUser.getFirstName());
        assertEquals(UserRole.CHEF_DE_PARTIE, updatedUser.getUserRole());
        assertEquals(Station.COLD, updatedUser.getStation());
        assertEquals(createdUser.getId(), updatedUser.getId());

    }

    @Test
    @DisplayName("Test delete user")
    void delete()
    {
        IUserDAO.create(user);

        boolean deleted = IUserDAO.delete(user.getId());
        assertTrue(deleted);

        Optional<User> found = IUserDAO.findById(user.getId());
        assertFalse(found.isPresent());
    }

    @AfterAll
    static void tearDown()
    {
        if (emf != null) emf.close();
    }
}
