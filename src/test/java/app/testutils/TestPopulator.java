package app.testutils;

import app.enums.MenuStatus;
import app.enums.RequestType;
import app.enums.UserRole;
import app.persistence.daos.*;
import app.persistence.entities.*;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TestPopulator
{
    private final IStationDAO stationDAO;
    private final IUserDAO userDAO;
    private final IAllergenDAO allergenDAO;
    private final IDishSuggestionDAO dishDAO;
    private final IWeeklyMenuDAO menuDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IShoppingListDAO shoppingListDAO;
    private Map<String, IEntity> seeded;

    public TestPopulator(EntityManagerFactory emf) {
        this.stationDAO = new StationDAO(emf);
        this.userDAO = new UserDAO(emf);
        this.allergenDAO = new AllergenDAO(emf);
        this.dishDAO = new DishSuggestionDAO(emf);
        this.menuDAO = new WeeklyMenuDAO(emf);
        this.ingredientRequestDAO = new IngredientRequestDAO(emf);
        this.shoppingListDAO = new ShoppingListDAO(emf);
        this.seeded = new HashMap<>();
    }

    public void populate()
    {
        populateStations();
        populateUsers();
        populateAllergens();
        populateDishSuggestions();
        populateIngredientRequest();
    }

    public Map<String, IEntity> getSeededData()
    {
        return seeded;
    }

    private void populateStations()
    {
        Station s1 = new Station("Cold Kitchen", "Salads & Starters");
        Station s2 = new Station("Hot Kitchen", "Main Courses");
        Station s3 = new Station("Pastry", "Desserts & Bread");
        Station s4 = new Station("Grill", "Steaks & BBQ");

        stationDAO.create(s1);
        stationDAO.create(s2);
        stationDAO.create(s3);
        stationDAO.create(s4);

        seeded.put("station_cold", s1);
        seeded.put("station_hot", s2);
        seeded.put("station_pastry", s3);
        seeded.put("station_grill", s4);
    }

    private void populateUsers()
    {
        User u1 = new User("Gordon", "Ramsay", "gordon@kitchen.com", "hash1", UserRole.HEAD_CHEF);
        User u2 = new User("Claire", "Smyth", "claire@pastry.com", "hash2", UserRole.LINE_COOK);
        User u3 = new User("Marco", "Pierre", "marco@grill.com", "hash3", UserRole.LINE_COOK);
        User u4 = new User("Rene", "Redzepi", "lars@cold.com", "hash4", UserRole.LINE_COOK);

        userDAO.create(u1);
        userDAO.create(u2);
        userDAO.create(u3);
        userDAO.create(u4);

        seeded.put("user_gordon", u1);
        seeded.put("user_claire", u2);
        seeded.put("user_marco", u3);
        seeded.put("user_rene", u4);
    }

    private void populateAllergens() {
        Allergen gluten = allergenDAO.create(new Allergen("Gluten", "🌾"));
        Allergen dairy = allergenDAO.create(new Allergen("Dairy", "🥛"));
        Allergen eggs = allergenDAO.create(new Allergen("Eggs", "🥚"));
        Allergen nuts = allergenDAO.create(new Allergen("Nuts", "🥜"));
        Allergen shellfish = allergenDAO.create(new Allergen("Shellfish", "🦐"));
        Allergen fish = allergenDAO.create(new Allergen("Fish", "🐟"));
        Allergen soy = allergenDAO.create(new Allergen("Soy", "🫘"));
        Allergen celery = allergenDAO.create(new Allergen("Celery", "🥬"));
        Allergen mustard = allergenDAO.create(new Allergen("Mustard", "🌭"));

        seeded.put("allergen_gluten", gluten);
        seeded.put("allergen_dairy", dairy);
        seeded.put("allergen_eggs", eggs);
        seeded.put("allergen_nuts", nuts);
        seeded.put("allergen_shellfish", shellfish);
        seeded.put("allergen_fish", fish);
        seeded.put("allergen_soy", soy);
        seeded.put("allergen_celery", celery);
        seeded.put("allergen_mustard", mustard);
    }

    private void populateDishSuggestions()
    {
        Station hotStation = (Station) seeded.get("station_hot");
        Station coldStation = (Station) seeded.get("station_cold");

        User cookClaire = (User) seeded.get("user_claire");
        User cookMarco = (User) seeded.get("user_marco");

        DishSuggestion d1 = new DishSuggestion(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            coldStation,
            cookClaire
        );

        DishSuggestion d2 = new DishSuggestion(
            "Bøf Bearnaise",
            "Oksemørbrad med hjemmelavet bearnaise",
            hotStation,
            cookMarco
        );

        DishSuggestion d3 = new DishSuggestion(
            "Tarteletter",
            "Høns i asparges",
            hotStation,
            cookClaire
        );

        DishSuggestion d4 = new DishSuggestion(
            "Roastbeef",
            "Roastbeef med remoulade, syltet agurk og sprøde løg",
            coldStation,
            cookMarco
        );

        dishDAO.create(d1);
        dishDAO.create(d2);
        dishDAO.create(d3);
        dishDAO.create(d4);

        seeded.put("dish_salmon", d1);
        seeded.put("dish_steak", d2);
        seeded.put("dish_tartelet", d3);
        seeded.put("dish_roastbeef", d4);
    }

    private void populateIngredientRequest()
    {
        User headChef = (User) seeded.get("user_gordon");
        User lineCook = (User) seeded.get("user_claire");
        DishSuggestion dish = (DishSuggestion) seeded.get("dish_salmon_pending");

        IngredientRequest req1 = new IngredientRequest(
            "Frisk Dild",
            10.0,
            "Bundter",
            "Grønttorvet",
            "Skal bruges til laksen",
            RequestType.DISH_SPECIFIC,
            LocalDate.now().plusDays(2),
            dish,
            lineCook
        );

        IngredientRequest req2 = new IngredientRequest(
            "Hvedemel Type 00",
            25.0,
            "kg",
            "Valsemøllen",
            "Lageret er næsten tomt",
            RequestType.GENERAL_STOCK,
            LocalDate.now().plusDays(5),
            null,
            headChef
        );

        IngredientRequest req3 = new IngredientRequest(
            "Trøffelolie",
            2.0,
            "Liter",
            "Inco",
            "Haster til weekendmenuen",
            RequestType.GENERAL_STOCK,
            LocalDate.now().plusDays(1),
            null,
            lineCook
        );

        ingredientRequestDAO.create(req1);
        ingredientRequestDAO.create(req2);
        ingredientRequestDAO.create(req3);

        seeded.put("req_dill", req1);
        seeded.put("req_flour", req2);
        seeded.put("req_truffle", req3);
    }

    private void populateWeeklyMenus() {
        User gordon = (User) seeded.get("user_gordon");

        WeeklyMenu menu10 = new WeeklyMenu(
            10,
            2025,
            MenuStatus.PUBLISHED,
        );

        // DRAFT MENU (Week 11 - Next week, being prepared)
        WeeklyMenu menu11 = menuDAO.create(new WeeklyMenu(
            11,
            2025,
            MenuStatus.DRAFT,
            null,
            null
        ));

        seeded.put("menu_week10", menu10);
        seeded.put("menu_week11", menu11);
    }



}
