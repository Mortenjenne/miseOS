package app.daos;

import app.config.HibernateTestConfig;
import app.enums.UserRole;
import app.persistence.daos.IUserDAO;
import app.persistence.daos.UserDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IUserDAO userDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        userDAO = new UserDAO(emf);
    }

    @AfterAll
    void tearDown()
    {
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }

    @DisplayName("Create - Should persist user")
    @Test
    void create()
    {
        User newUser = new User("Jamie", "Oliver", "jamie@chef.com", "hash", UserRole.LINE_COOK);

        User result = userDAO.create(newUser);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getUserRole(), is(UserRole.LINE_COOK));
        assertThat(result.getFirstName(), is("Jamie"));
        assertThat(result.getLastName(), is("Oliver"));
    }

    @DisplayName("Create - should throw exception when user is null")
    @Test
    void createNullUserThrowsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userDAO.create(null));

        assertTrue(exception.getMessage().contains("User cannot be null"));
    }

    @Test
    @DisplayName("Create - should throw exception when email is duplicate")
    void create_DuplicateEmail_ThrowsException() {

        User gordon = (User) seeded.get("user_gordon");
        User duplicate = new User("Fake", "Gordon", gordon.getEmail(), "hash", UserRole.LINE_COOK);

        assertThrows(Exception.class, () -> userDAO.create(duplicate));
    }

    @DisplayName("Test retrieving all users")
    @Test
    void getAll()
    {
        Set<User> users = userDAO.getAll();

        assertThat(users, hasSize(4));
        assertThat(users, containsInAnyOrder(seeded.get("user_gordon"), seeded.get("user_claire"), seeded.get("user_marco"), seeded.get("user_rene")));
    }

    @DisplayName("Get by id - Should retrieve user")
    @Test
    void getByID()
    {
        User seed = (User) seeded.get("user_gordon");
        User fetched = userDAO.getByID(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getFirstName(), is(seed.getFirstName()));
        assertThat(fetched.getLastName(), is(seed.getLastName()));
        assertThat(fetched.getEmail(), is(seed.getEmail()));
        assertThat(fetched.getUserRole(), is(seed.getUserRole()));
    }

    @DisplayName("Get by id - Should throw EntityNotFoundException when user doesn't exist")
    @Test
    void getByIDNotFoundThrowsException() {

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userDAO.getByID(999L));

        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @DisplayName("Test updating a user")
    @Test
    void update()
    {
        User seed = (User) seeded.get("user_gordon");
        seed.setFirstName("Dak");
        seed.setLastName("Wichangoen");
        seed.setUserRole(UserRole.SOUS_CHEF);

        User updated = userDAO.update(seed);

        assertThat(updated.getId(), is(seed.getId()));
        assertThat(updated.getFirstName(), is("Dak"));
        assertThat(updated.getUserRole(), is(UserRole.SOUS_CHEF));

        User fetched = userDAO.getByID(seed.getId());
        assertThat(fetched.getLastName(), is("Wichangoen"));
    }

    @DisplayName("Test deleting a user")
    @Test
    void delete()
    {
        User seed = (User) seeded.get("user_rene");
        Long id = seed.getId();

        boolean isDeleted = userDAO.delete(id);
        Set<User> users = userDAO.getAll();

        assertThat(isDeleted, is(true));
        assertThrows(EntityNotFoundException.class, () -> userDAO.getByID(id));
        assertThat(users, hasSize(3));
    }

    @DisplayName("Test finding user by email")
    @Test
    void findByEmail()
    {
        User seed = (User) seeded.get("user_claire");

        Optional<User> fetched = userDAO.findByEmail(seed.getEmail());

        assertTrue(fetched.isPresent());
        User user = fetched.get();
        assertThat(user.getEmail(), is(seed.getEmail()));
        assertThat(user.getId(), is(seed.getId()));
    }

    @DisplayName("Test finding user by email not present")
    @Test
    void findByEmailNotPresent()
    {
        String email = "fake@email.dk";

        Optional<User> fetched = userDAO.findByEmail(email);

        assertTrue(fetched.isEmpty());
    }

    @Test
    void findByRole()
    {
        Set<User> cooks = userDAO.findByRole(UserRole.LINE_COOK);

        assertThat(cooks, hasSize(3));
        cooks.forEach(u -> assertThat(u.getUserRole(), is(UserRole.LINE_COOK)));
    }

    @Test
    void existsByEmail()
    {
        User seed = (User) seeded.get("user_gordon");
        String nonExistingEmail = "fake@chef.com";

        assertThat(userDAO.existsByEmail(seed.getEmail()), is(true));
        assertThat(userDAO.existsByEmail(nonExistingEmail), is(false));
    }
}
