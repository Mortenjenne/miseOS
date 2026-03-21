package app.config;

import app.controllers.*;
import app.integrations.ai.GeminiClient;
import app.integrations.ai.IAiClient;
import app.integrations.translation.DeepLTranslationClient;
import app.integrations.translation.ITranslationClient;
import app.integrations.weather.WeatherClient;
import app.persistence.daos.impl.*;
import app.persistence.daos.interfaces.*;
import app.services.*;
import app.services.impl.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;

import java.net.http.HttpClient;
import java.time.Duration;

public final class DIContainer
{
    private static DIContainer instance;
    private final EntityManagerFactory emf;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;

    private final ITranslationClient translationClient;
    private final IAiClient aiClient;
    private final WeatherClient weatherClient;

    private final IAllergenDAO allergenDAO;
    private final IUserDAO userDAO;
    private final IDishDAO dishDAO;
    private final IDishSuggestionDAO dishSuggestionDAO;
    private final IWeeklyMenuDAO weeklyMenuDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IShoppingListDAO shoppingListDAO;
    private final IStationDAO stationDAO;

    private final IAllergenService allergenService;
    private final IDishService dishService;
    private final IDishSuggestionService dishSuggestionService;
    private final IUserService userService;
    private final IWeeklyMenuService weeklyMenuService;
    private final IIngredientRequestService ingredientRequestService;
    private final IShoppingListService shoppingListService;
    private final IStationService stationService;
    private final IAiService aiService;
    private final IDishTranslationService dishTranslationService;
    private final IMenuInspirationService menuInspirationService;
    private final NotificationService notificationService;
    private final INotificationSnapshotService notificationSnapshotService;
    private final ISecurityService securityService;

    @Getter
    private final IAllergenController allergenController;

    @Getter
    private final IStationController stationController;

    @Getter
    private final IUserController userController;

    @Getter
    private final IMenuInspirationController menuInspirationController;

    @Getter
    private final IDishSuggestionController dishSuggestionController;

    @Getter
    private final IDishController dishController;

    @Getter
    private final IWeeklyMenuController weeklyMenuController;

    @Getter
    private final IIngredientRequestController ingredientRequestController;

    @Getter
    private final IShoppingListController shoppingListController;

    @Getter
    private final INotificationController notificationController;

    @Getter
    private final IExceptionController exceptionController;

    @Getter
    private final ISecurityController securityController;


    private DIContainer(EntityManagerFactory emf)
    {
        this.emf = emf;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        this.objectMapper = ObjectMapperConfig.create();
        this.apiConfig = new ApiConfig();

        this.translationClient = new DeepLTranslationClient(httpClient, objectMapper, apiConfig.getDeepLUrl(), apiConfig.getDeepLApiKey());
        this.aiClient = new GeminiClient(httpClient, objectMapper, apiConfig.getGeminiApiKey(), apiConfig.getGeminiUrl());
        this.weatherClient = new WeatherClient(httpClient, objectMapper, apiConfig.getOpenMeteoUrl());

        this.allergenDAO = new AllergenDAO(emf);
        this.userDAO = new UserDAO(emf);
        this.dishDAO = new DishDAO(emf);
        this.dishSuggestionDAO = new DishSuggestionDAO(emf);
        this.weeklyMenuDAO = new WeeklyMenuDAO(emf);
        this.ingredientRequestDAO = new IngredientRequestDAO(emf);
        this.shoppingListDAO = new ShoppingListDAO(emf);
        this.stationDAO = new StationDAO(emf);

        this.notificationService = new NotificationService();
        this.notificationSnapshotService = new NotificationSnapshotService(dishSuggestionDAO, ingredientRequestDAO);
        this.dishTranslationService = new DishTranslationService(translationClient);
        this.aiService = new AiService(objectMapper, aiClient);
        this.allergenService = new AllergenService(allergenDAO);
        this.stationService = new StationService(stationDAO);
        this.dishService = new DishService(dishDAO, allergenDAO, stationDAO, userDAO);
        this.dishSuggestionService = new DishSuggestionService(dishSuggestionDAO, dishDAO, userDAO, stationDAO, allergenDAO, notificationService);
        this.userService = new UserService(userDAO, stationDAO);
        this.weeklyMenuService = new WeeklyMenuService(weeklyMenuDAO, dishDAO, userDAO, stationDAO, dishTranslationService);
        this.ingredientRequestService = new IngredientRequestService(ingredientRequestDAO, dishDAO, userDAO, notificationService);
        this.shoppingListService = new ShoppingListService(shoppingListDAO, ingredientRequestDAO, userDAO, aiService);
        this.menuInspirationService = new MenuInspirationService(aiService, userDAO, weatherClient);
        this.securityService = new SecurityService(userDAO, apiConfig.getIssuer(), apiConfig.getSecretKey(), apiConfig.getExpirationMs());

        this.allergenController = new AllergenController(allergenService);
        this.stationController = new StationController(stationService);
        this.userController = new UserController(userService);
        this.menuInspirationController = new MenuInspirationController(menuInspirationService);
        this.dishSuggestionController = new DishSuggestionController(dishSuggestionService);
        this.dishController = new DishController(dishService);
        this.weeklyMenuController = new WeeklyMenuController(weeklyMenuService);
        this.ingredientRequestController = new IngredientRequestController(ingredientRequestService);
        this.shoppingListController = new ShoppingListController(shoppingListService);
        this.notificationController = new NotificationController(notificationService, notificationSnapshotService);
        this.exceptionController = new ExceptionController();
        this.securityController = new SecurityController(securityService);
    }

    public static DIContainer getInstance()
    {
        if (instance == null)
        {
            instance = new DIContainer(HibernateConfig.getEntityManagerFactory());
        }
        return instance;
    }

    public static DIContainer getTestInstance(EntityManagerFactory emf)
    {
        instance = new DIContainer(emf);
        return instance;
    }
}
