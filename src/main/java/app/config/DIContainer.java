package app.config;

import app.controllers.AllergenController;
import app.controllers.IAllergenController;
import app.controllers.IUserController;
import app.controllers.UserController;
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

public final class DIContainer
{
    @Getter
    private static final DIContainer instance = new DIContainer();

    private final EntityManagerFactory emf;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

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

    private final IAllergenController allergenController;
    //private final IDishController dishController;
    //private final IDishSuggestionController dishSuggestionController;
    private final IUserController userController;
    //private final IWeeklyMenuController weeklyMenuController;
    //private final IIngredientRequestController ingredientRequestController;
    //private final IShoppingListController shoppingListController;
    //private final IStationController stationController;

    private DIContainer()
    {
        this.emf          = HibernateConfig.getEntityManagerFactory();
        this.httpClient   = HttpClient.newHttpClient();
        this.objectMapper = ObjectMapperConfig.create();

        this.translationClient = new DeepLTranslationClient(httpClient, objectMapper, AppProperties.get("DEEPL_URL"), System.getenv("DEEPL_APIKEY"));
        this.aiClient = new GeminiClient(httpClient, objectMapper, System.getenv("GEMINI_API_KEY"), AppProperties.get("GEMINI_URL"));
        this.weatherClient = new WeatherClient(httpClient, objectMapper, AppProperties.get("OPEN_METEO_URL"));

        this.allergenDAO = new AllergenDAO(emf);
        this.userDAO = new UserDAO(emf);
        this.dishDAO = new DishDAO(emf);
        this.dishSuggestionDAO = new DishSuggestionDAO(emf);
        this.weeklyMenuDAO = new WeeklyMenuDAO(emf);
        this.ingredientRequestDAO = new IngredientRequestDAO(emf);
        this.shoppingListDAO = new ShoppingListDAO(emf);
        this.stationDAO = new StationDAO(emf);

        this.dishTranslationService = new DishTranslationService(translationClient);
        this.aiService = new AiService(objectMapper, aiClient);
        this.allergenService = new AllergenService(allergenDAO, userDAO);
        this.stationService = new StationService(stationDAO, userDAO);
        this.dishService = new DishService(dishDAO, allergenDAO, stationDAO, userDAO);
        this.dishSuggestionService = new DishSuggestionService(dishSuggestionDAO, dishDAO, userDAO, stationDAO, allergenDAO);
        this.userService = new UserService(userDAO, stationDAO);
        this.weeklyMenuService = new WeeklyMenuService(weeklyMenuDAO, dishDAO, userDAO, stationDAO, dishTranslationService);
        this.ingredientRequestService = new IngredientRequestService(ingredientRequestDAO, dishDAO, userDAO);
        this.shoppingListService = new ShoppingListService(shoppingListDAO, ingredientRequestDAO, userDAO, aiService);

        this.allergenController = new AllergenController(allergenService);
//        this.dishController = new DishController(dishService);
//        this.dishSuggestionController = new DishSuggestionController(dishSuggestionService);
        this.userController = new UserController(userService);
//        this.weeklyMenuController = new WeeklyMenuController(weeklyMenuService);
//        this.ingredientRequestController = new IngredientRequestController(ingredientRequestService);
//        this.shoppingListController = new ShoppingListController(shoppingListService);
//        this.stationController = new StationController(stationService);
    }

    public IAllergenController getAllergenController() { return allergenController; }
//    public IDishController getDishController() { return dishController; }
//    public IDishSuggestionController getDishSuggestionController() { return dishSuggestionController; }
    public IUserController getUserController() { return userController; }
//    public IWeeklyMenuController getWeeklyMenuController() { return weeklyMenuController; }
//    public IIngredientRequestController getIngredientRequestController() { return ingredientRequestController; }
//    public IShoppingListController getShoppingListController() { return shoppingListController; }
//    public IStationController getStationController() { return stationController; }
}
