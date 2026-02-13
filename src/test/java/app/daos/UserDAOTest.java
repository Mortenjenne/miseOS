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
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.hamcrest.Matchers.*;

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
        seeded = populator.populate();
        userDAO = new UserDAO(emf);
    }

    @AfterAll
    void tearDown()
    {
        emf.close();
    }

    @DisplayName("Test creating user")
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

    @DisplayName("Test retrieving all users")
    @Test
    void getAll()
    {
        Set<User> users = userDAO.getAll();

        assertThat(users, hasSize(4));
        assertThat(users, containsInAnyOrder(seeded.get("user1"), seeded.get("user2"), seeded.get("user3"), seeded.get("user4")));
    }

    @DisplayName("Test retrieving user by user id")
    @Test
    void getByID()
    {
        User seed = (User) seeded.get("user1");
        User fetched = userDAO.getByID(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getFirstName(), is(seed.getFirstName()));
        assertThat(fetched.getLastName(), is(seed.getLastName()));
        assertThat(fetched.getEmail(), is(seed.getEmail()));
        assertThat(fetched.getUserRole(), is(seed.getUserRole()));
    }

    @DisplayName("Test updating a user")
    @Test
    void update()
    {
        User seed = (User) seeded.get("user1");
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
        User seed = (User) seeded.get("user1");
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
        User seed = (User) seeded.get("user2");

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
        User seed = (User) seeded.get("user1");
        String nonExistingEmail = "fake@chef.com";

        assertThat(userDAO.existsByEmail(seed.getEmail()), is(true));
        assertThat(userDAO.existsByEmail(nonExistingEmail), is(false));
    }
}
