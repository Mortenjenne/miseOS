# Coding Standards for MiseOS Project

**Version:** 1.0  
**Date:** 2026-02-06  
**Project:** MiseOS (Kitchen Menu Management System)  
**Semester:** 3rd Semester Backend Portfolio  
**Duration:** 10 weeks backend + 4 weeks frontend

---

## 1. Introduction

This document describes the coding standards and best practices to be followed in the MiseOS project. These standards ensure consistent, readable, and maintainable code throughout the development process.

**Philosophy:** Clean code, SOLID principles, and production-ready architecture from day one.

---

## 2. Technology Stack

### Backend (Weeks 1-10)
- **Language:** Java 17+
- **Framework:** Jakarta EE / Hibernate
- **ORM:** Hibernate 6.x with JPA
- **Database:** PostgreSQL
- **Connection Pool:** HikariCP (via Hibernate)
- **Architecture:** Layered Architecture (Controller → Service → Repository → Entity)
- **Build Tool:** Maven
- **Version Control:** Git/GitHub
- **Testing Framework:** JUnit 5
- **IDE:** IntelliJ IDEA (recommended)

### Frontend (Weeks 11-14)
- **Framework:** React
- **Routing:** React Router
- **Styling:** CSS Modules
- **Build:** Vite or Create React App
- **Deployment:** Digital Ocean Droplet
- **CI/CD:** GitHub Actions
- **Containerization:** Docker + Docker Compose
- **Reverse Proxy:** Caddy (HTTPS)
- **Auto-deployment:** Watchtower

---

## 3. Editor and Formatting

### 3.1 EditorConfig
- Project contains an `.editorconfig` file in the root directory
- ALL developers MUST use these settings
- Ensure your IDE/editor supports EditorConfig
- Install EditorConfig plugin if necessary

### 3.2 General Formatting Rules
- **Indentation:** As specified in `.editorconfig` (typically 4 spaces for Java)
- **Line Endings:** Unix-style (LF)
- **Encoding:** UTF-8
- **Max Line Length:** 120 characters
- **Trailing Whitespace:** Remove all trailing whitespace

---

## 4. Naming Conventions

### 4.1 Classes
- **PascalCase** (uppercase first letter)
- Names should be nouns and descriptive
- **Examples:**
    - Entities: `User`, `Dish`, `WeeklyMenu`, `MenuItem`
    - Repositories: `UserRepository`, `DishRepository`
    - Services: `MenuService`, `DishService`
    - Controllers: `MenuController`, `AuthController`
    - DTOs: `UserDTO`, `DishResponseDTO`, `CreateMenuRequestDTO`

### 4.2 Interfaces
- **PascalCase**
- Do NOT prefix with 'I' (modern Java convention)
- **Examples:** `UserRepository`, `AuthenticationService`

### 4.3 Methods
- **camelCase** (lowercase first letter)
- Names should be verbs or verb phrases
- **Examples:**
    - `createWeeklyMenu()`, `approveDish()`, `publishMenu()`
    - `findByWeekAndYear()`, `getAllPendingSuggestions()`
    - `calculateTotalCost()`, `validateAllergens()`

### 4.4 Variables
- **camelCase**
- Descriptive names - avoid abbreviations unless universally understood
- Constants in **UPPER_SNAKE_CASE**
- **Examples:**
    - Variables: `weeklyMenu`, `dishSuggestion`, `ingredientRequest`
    - Constants: `MAX_MENU_ITEMS`, `DEFAULT_PAGE_SIZE`, `API_VERSION`

### 4.5 Packages
- **lowercase**, only lowercase letters
- Follow domain structure
- **Structure:**
  ```
  app
  ├── config          (Hibernate configuration)
  ├── controllers     (REST endpoints)
  ├── services        (Business logic)
  ├── repositories    (Data access layer)
  ├── entities        (JPA entities)
  ├── dtos            (Data Transfer Objects)
  ├── exceptions      (Custom exceptions)
  ├── enums           (Enumerations)
  ├── security        (JWT, authentication)
  └── utils           (Utility classes)
  ```

---

## 5. Architecture (Layered Architecture)

### 5.1 Entities (Domain Layer)
- **Location:** `app.entities` package
- **Purpose:** JPA entities representing database tables
- **Rules:**
    - Annotated with `@Entity`, `@Table`, etc.
    - Contains only domain data and simple getters/setters
    - May contain domain logic methods (e.g., `isPublished()`, `canBeEdited()`)
    - NO business logic that depends on external services
    - Use appropriate JPA annotations (`@OneToMany`, `@ManyToOne`, etc.)

**Example:**
```java
@Entity
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private DishStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    
    // Domain logic is OK
    public boolean isPending() {
        return status == DishStatus.PENDING;
    }
}
```

### 5.2 Repositories (Data Access Layer)
- **Location:** `app.repositories` package
- **Purpose:** Responsible for database operations
- **Rules:**
    - Use `EntityManager` (injected via constructor)
    - Use dependency injection for `EntityManagerFactory`
    - Follow repository pattern
    - Return entities or `Optional<Entity>`
    - Throw `PersistenceException` or custom exceptions on errors

**Example:**
```java
public class DishRepository {
    private final EntityManagerFactory emf;
    
    // Dependency injection via constructor
    public DishRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }
    
    public Optional<Dish> findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            Dish dish = em.find(Dish.class, id);
            return Optional.ofNullable(dish);
        }
    }
    
    public List<Dish> findByStatus(DishStatus status) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT d FROM Dish d WHERE d.status = :status", 
                Dish.class
            )
            .setParameter("status", status)
            .getResultList();
        }
    }
}
```

### 5.3 Services (Business Logic Layer)
- **Location:** `app.services` package
- **Purpose:** Contains business logic and orchestrates workflows
- **Rules:**
    - Inject repositories via constructor
    - Handle transactions using `EntityManager.getTransaction()`
    - Validate business rules
    - Transform entities to DTOs (do not expose entities to controllers)
    - Throw custom business exceptions

**Example:**
```java
public class MenuService {
    private final WeeklyMenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final EntityManagerFactory emf;
    
    public MenuService(WeeklyMenuRepository menuRepository, 
                       DishRepository dishRepository,
                       EntityManagerFactory emf) {
        this.menuRepository = menuRepository;
        this.dishRepository = dishRepository;
        this.emf = emf;
    }
    
    public void publishMenu(Long menuId) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                WeeklyMenu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new MenuNotFoundException(menuId));
                
                if (menu.isPublished()) {
                    throw new MenuAlreadyPublishedException(menuId);
                }
                
                menu.publish(); // Domain method
                em.merge(menu);
                
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }
}
```

### 5.4 Controllers (Presentation Layer)
- **Location:** `app.controllers` package
- **Purpose:** Handle HTTP requests and responses
- **Rules:**
    - Use Javalin or similar framework
    - Inject services via constructor
    - Parse request data and call services
    - Return DTOs, never entities
    - Handle authentication/authorization
    - Catch exceptions and return appropriate HTTP status codes

**Example:**
```java
public class MenuController {
    private final MenuService menuService;
    
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    public void publishMenu(Context ctx) {
        Long menuId = ctx.pathParamAsClass("id", Long.class).get();
        
        try {
            menuService.publishMenu(menuId);
            ctx.status(200).json(Map.of("message", "Menu published successfully"));
        } catch (MenuNotFoundException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (MenuAlreadyPublishedException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }
}
```

### 5.5 DTOs (Data Transfer Objects)
- **Location:** `app.dtos` package
- **Purpose:** Transfer data between layers (especially API ↔ Service)
- **Rules:**
    - Separate DTOs for requests and responses
    - No business logic
    - May include validation annotations
    - Use records (Java 17+) when immutability is desired

**Example:**
```java
public record CreateDishRequest(
    @NotBlank String name,
    @NotBlank String description,
    @NotNull Set<Allergen> allergens,
    @NotNull Station station
) {}

public record DishResponse(
    Long id,
    String name,
    String description,
    Set<Allergen> allergens,
    DishStatus status,
    String createdBy
) {}
```

---

## 6. Dependency Injection

### 6.1 EntityManagerFactory Injection
- **ALWAYS** use dependency injection for `EntityManagerFactory`
- Enables testing with test databases
- Constructor injection is the preferred method
- Use HibernateConfig pattern as shown in project setup

**HibernateConfig Pattern:**
```java
// HibernateConfig.java - Singleton factory
public final class HibernateConfig {
    private static volatile EntityManagerFactory emf;
    
    private HibernateConfig() {}
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (HibernateConfig.class) {
                if (emf == null) {
                    emf = HibernateEmfBuilder.build(buildProps());
                }
            }
        }
        return emf;
    }
    
    private static Properties buildProps() {
        Properties props = HibernateBaseProperties.createBase();
        props.put("hibernate.hbm2ddl.auto", "update"); // Use 'validate' in production
        
        if (System.getenv("DEPLOYED") != null) {
            setDeployedProperties(props);
        } else {
            setDevProperties(props);
        }
        return props;
    }
    
    private static void setDeployedProperties(Properties props) {
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR"));
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
    }
    
    private static void setDevProperties(Properties props) {
        String dbName = Utils.getPropertyValue("DB_NAME", "config.properties");
        String username = Utils.getPropertyValue("DB_USERNAME", "config.properties");
        String password = Utils.getPropertyValue("DB_PASSWORD", "config.properties");
        
        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/" + dbName);
        props.put("hibernate.connection.username", username);
        props.put("hibernate.connection.password", password);
    }
}
```

### 6.2 Entity Registration
- Register all entities in `EntityRegistry.java`
- Keep entity registration separate from configuration

**Example:**
```java
final class EntityRegistry {
    private EntityRegistry() {}
    
    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Dish.class);
        configuration.addAnnotatedClass(WeeklyMenu.class);
        configuration.addAnnotatedClass(MenuItem.class);
        configuration.addAnnotatedClass(IngredientRequest.class);
    }
}
```

### 6.3 Layer Decoupling
- Controllers depend on Services (not Repositories)
- Services depend on Repositories
- Repositories depend only on EntityManagerFactory
- NO layer should skip levels (Controller → Repository is forbidden)

---

## 7. Comments and Documentation

### 7.1 When to Comment
- Explain **WHY**, not **WHAT**
- Complex business logic that isn't obvious
- Public APIs (use Javadoc)
- Non-obvious technical decisions

### 7.2 When NOT to Comment
- Self-explanatory code
- Trivial getters/setters
- Obvious implementations

### 7.3 Javadoc
- Use for public APIs and service methods
- Include `@param`, `@return`, `@throws` tags

**Example:**
```java
/**
 * Publishes a weekly menu, making it immutable.
 * All associated menu items become snapshots.
 *
 * @param menuId the ID of the menu to publish
 * @throws MenuNotFoundException if menu doesn't exist
 * @throws MenuAlreadyPublishedException if menu is already published
 */
public void publishMenu(Long menuId) {
    // implementation
}
```

---

## 8. Exception Handling

### 8.1 General Rules
- NEVER catch exceptions without handling them
- NEVER use empty catch blocks
- Log exceptions appropriately
- Throw exceptions up when it makes sense
- Use try-with-resources for `EntityManager`

### 8.2 Custom Exceptions
- Create domain-specific exceptions
- Place in `app.exceptions` package
- Extend `RuntimeException` for business logic exceptions
- Include meaningful messages

**Example:**
```java
public class MenuNotFoundException extends RuntimeException {
    public MenuNotFoundException(Long menuId) {
        super("Menu with ID " + menuId + " not found");
    }
}

public class MenuAlreadyPublishedException extends RuntimeException {
    public MenuAlreadyPublishedException(Long menuId) {
        super("Menu with ID " + menuId + " is already published");
    }
}
```

### 8.3 Transaction Handling
- Always use try-catch-rollback pattern for transactions

**Example:**
```java
try (EntityManager em = emf.createEntityManager()) {
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    try {
        // Database operations
        tx.commit();
    } catch (Exception e) {
        tx.rollback();
        throw new PersistenceException("Failed to save dish", e);
    }
}
```

---

## 9. Database Operations

### 9.1 JPQL Queries
- Use named parameters (not positional)
- Avoid native SQL unless absolutely necessary
- Use type-safe queries

**Example:**
```java
em.createQuery("SELECT d FROM Dish d WHERE d.station = :station", Dish.class)
  .setParameter("station", station)
  .getResultList();
```

### 9.2 Resource Management
- ALWAYS use try-with-resources for `EntityManager`
- Close resources in finally block if try-with-resources isn't possible
- Never leak EntityManager instances

### 9.3 Lazy Loading
- Understand `FetchType.LAZY` vs `FetchType.EAGER`
- Avoid N+1 query problems
- Use JOIN FETCH when appropriate

### 9.4 Cascade Operations
- Use `CascadeType` carefully
- Document why specific cascade types are chosen
- Be cautious with `CascadeType.ALL`

---

## 10. Testing

### 10.1 Unit Tests
- Test business logic in services
- Use JUnit 5
- Mock repositories using Mockito
- Place tests in `src/test/java` with same package structure
- Naming convention: `ClassNameTest.java`

**Example:**
```java
class MenuServiceTest {
    
    @Test
    void publishMenu_shouldSetStatusToPublished() {
        // Arrange
        WeeklyMenu menu = new WeeklyMenu();
        menu.setStatus(MenuStatus.DRAFT);
        
        // Act
        menu.publish();
        
        // Assert
        assertEquals(MenuStatus.PUBLISHED, menu.getStatus());
    }
}
```

### 10.2 Integration Tests
- Test repository layer with actual database
- Use test database (separate from development)
- Create test-specific `HibernateConfig`
- Clean database between tests
- Naming convention: `ClassNameIntegrationTest.java`

**Example:**
```java
class DishRepositoryIntegrationTest {
    
    private static EntityManagerFactory testEmf;
    private DishRepository repository;
    
    @BeforeAll
    static void setupDatabase() {
        testEmf = HibernateConfig.getEntityManagerFactoryForTest();
    }
    
    @BeforeEach
    void setup() {
        repository = new DishRepository(testEmf);
        cleanDatabase();
    }
    
    @Test
    void findByStatus_shouldReturnOnlyPendingDishes() {
        // Test implementation
    }
}
```

---

## 11. Git Workflow and Branching

### 11.1 Branch Strategy
- **main:** Production-ready code (protected)
- **dev:** Development branch (pull requests required)
- **feature/[feature-name]:** Feature branches
    - Example: `feature/dish-approval-workflow`
    - Example: `feature/ingredient-requests`

### 11.2 Commit Messages
- Write clear, descriptive commit messages in English
- Use imperative mood: "Add feature" not "Added feature"
- First line: summary (max 50 chars)
- Optional body: detailed explanation
- Reference issue numbers if applicable

**Good Examples:**
```
Add Dish entity with JPA annotations
Implement menu approval workflow
Fix N+1 query in WeeklyMenuRepository
```

**Bad Examples:**
```
Updates
fix
asdf
minor changes
```

### 11.3 Commit Frequency
- **Minimum:** Commit at least once per work session
- **Recommended:** Commit after each logical change
- **Purpose:** Ensure backup and maintain clear history
- Push to remote repository regularly

### 11.4 Pull Requests
- ALL merges to dev must go through pull request
- Self-review before creating PR
- Run all tests before creating PR
- Include description of changes
- Reference related issues/user stories

### 11.5 Merge Conflicts
- Resolve in feature branch before PR
- Never commit conflict markers
- Test after resolving conflicts

---

## 12. Configuration Management

### 12.1 Environment-Specific Configuration
- Use `config.properties` for local development
- Use environment variables for deployment
- NEVER commit sensitive data (passwords, API keys)
- Add `config.properties` to `.gitignore`

### 12.2 config.properties Template
Provide a `config.properties.template` in the repository:
```properties
DB_NAME=miseos_dev
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

Users copy this to `config.properties` and fill in their values.

---

## 13. Security Best Practices

### 13.1 Authentication
- Use JWT for stateless authentication
- Hash passwords with BCrypt (never store plain text)
- Implement proper session management

### 13.2 Authorization
- Check user roles before allowing actions
- Line Cooks can only edit their own suggestions
- Only Head Chef can publish menus

### 13.3 Input Validation
- Validate all user input
- Use bean validation annotations (`@NotNull`, `@NotBlank`, etc.)
- Sanitize inputs to prevent injection attacks

---

## 14. Code Review Checklist

Before creating a Pull Request, verify:

- [ ] Code follows naming conventions
- [ ] EditorConfig rules are followed
- [ ] Dependency injection is used properly
- [ ] EntityManager uses try-with-resources
- [ ] JPQL queries use named parameters
- [ ] Custom exceptions are used where appropriate
- [ ] Unit tests are written and passing
- [ ] Integration tests run successfully (if applicable)
- [ ] No sensitive data in code
- [ ] Commit messages are clear and descriptive
- [ ] No merge conflicts with dev branch
- [ ] Documentation is updated if needed

---

## 15. Weekly Development Workflow

### Week 1: Project Setup
- Set up GitHub repository
- Configure Hibernate with PostgreSQL
- Create initial entities
- Write first blog post

### Week 2-9: Incremental Development
- Each Friday: Dedicated project work
- Integrate week's new technology into project
- Write weekly blog post documenting progress

### Week 10: Backend Polish
- Code cleanup
- Documentation
- Deployment preparation

### Week 11-14: Frontend Development
- React application
- API integration
- CI/CD pipeline
- Final deployment

---

## 16. Portfolio Documentation

### 16.1 Weekly Blog Posts
- Document what was built this week
- Explain technology decisions
- Discuss challenges and solutions
- Reflect on learning outcomes

### 16.2 README.md
- Keep updated with:
    - Project description
    - Setup instructions
    - Technology stack
    - API documentation
    - Deployment guide

---

## 17. Production Readiness

### 17.1 Configuration
- Use `hibernate.hbm2ddl.auto=validate` in production
- Enable connection pooling (HikariCP configured)
- Set appropriate pool sizes
- Configure proper logging

### 17.2 Performance
- Use indexes on frequently queried columns
- Optimize N+1 queries
- Monitor query performance
- Implement caching if needed

### 17.3 Monitoring
- Log errors appropriately
- Include request tracking
- Monitor database connection pool

---

## 18. Resources and References

### Documentation
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [JPA Specification](https://jakarta.ee/specifications/persistence/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

### Learning Resources
- Effective Java (Joshua Bloch)
- Clean Code (Robert C. Martin)
- SOLID Principles

---

## 19. Contact and Support

**For questions or clarification:**
1. Consult this coding standards document
2. Check project documentation in README
3. Review existing code examples
4. Ask in team chat/discussion forum

---

**Last Updated:** 2026-02-06  
**Version:** 1.0  
**Maintainer:** [Your Name]
