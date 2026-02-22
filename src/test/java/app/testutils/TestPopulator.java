package app.testutils;

import app.enums.DayOfWeek;
import app.enums.RequestType;
import app.enums.Unit;
import app.enums.UserRole;
import app.persistence.daos.*;
import app.persistence.entities.*;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestPopulator
{
    private final IStationDAO stationDAO;
    private final IUserDAO userDAO;
    private final IAllergenDAO allergenDAO;
    private final IDishSuggestionDAO dishDAO;
    private final IWeeklyMenuDAO menuDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IShoppingListDAO shoppingListDAO;
    private final Map<String, IEntity> seeded;

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
        populateWeeklyMenus();
        populateShoppingLists();
        populateForGeminiIngredientRequest();
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
        User u4 = new User("Rene", "Redzepi", "rene@cold.com", "hash4", UserRole.LINE_COOK);

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
        Allergen gluten = allergenDAO.create(new Allergen("Gluten", "Cereals containing gluten", 1));
        Allergen dairy = allergenDAO.create(new Allergen("Dairy", "Milk and products thereof (including lactose)",2));
        Allergen eggs = allergenDAO.create(new Allergen("Eggs", "Eggs and products thereof",3));
        Allergen nuts = allergenDAO.create(new Allergen("Nuts", "Tree nuts", 4));
        Allergen shellfish = allergenDAO.create(new Allergen("Shellfish", "Fish and products thereof", 5));
        Allergen fish = allergenDAO.create(new Allergen("Fish", "Fish and products thereof", 6));
        Allergen soy = allergenDAO.create(new Allergen("Soy", "Cereals containing gluten", 7));
        Allergen celery = allergenDAO.create(new Allergen("Celery", "Celery and products thereof", 8));
        Allergen mustard = allergenDAO.create(new Allergen("Mustard", "Mustard and products thereof", 9));

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

        Allergen gluten = (Allergen) seeded.get("allergen_gluten");
        Allergen dairy = (Allergen) seeded.get("allergen_dairy");
        Allergen eggs = (Allergen) seeded.get("allergen_eggs");
        Allergen fish = (Allergen) seeded.get("allergen_fish");

        Set<Allergen> allergens = new HashSet<>();
        allergens.add(gluten);
        allergens.add(dairy);
        allergens.add(eggs);
        allergens.add(fish);

        DishSuggestion d1 = new DishSuggestion(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            7,
            2026,
            coldStation,
            cookClaire,
            allergens
        );

        DishSuggestion d2 = new DishSuggestion(
            "Bøf Bearnaise",
            "Oksemørbrad med hjemmelavet bearnaise",
            7,
            2026,
            hotStation,
            cookMarco,
            allergens
        );

        DishSuggestion d3 = new DishSuggestion(
            "Tarteletter",
            "Høns i asparges",
            7,
            2026,
            hotStation,
            cookClaire,
            allergens
        );

        DishSuggestion d4 = new DishSuggestion(
            "Roastbeef",
            "Roastbeef med remoulade, syltet agurk og sprøde løg",
            7,
            2026,
            coldStation,
            cookMarco,
            allergens
        );

        DishSuggestion d5 = new DishSuggestion(
            "Sushi",
            "sashimi og syltet ingefær",
            8,
            2026,
            coldStation,
            cookClaire,
            allergens
        );

        dishDAO.create(d1);
        dishDAO.create(d2);
        dishDAO.create(d3);
        dishDAO.create(d4);
        dishDAO.create(d5);

        seeded.put("dish_salmon", d1);
        seeded.put("dish_steak", d2);
        seeded.put("dish_tartelet", d3);
        seeded.put("dish_roastbeef", d4);
        seeded.put("dish_sushi",d5);
    }

    private void populateIngredientRequest()
    {
        User headChef = (User) seeded.get("user_gordon");
        User lineCook = (User) seeded.get("user_claire");
        DishSuggestion dish = (DishSuggestion) seeded.get("dish_salmon");

        IngredientRequest req1 = new IngredientRequest(
            "Frisk Dild",
            10.0,
            Unit.BUNCH,
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
            Unit.KG,
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
            Unit.L,
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

    private void populateForGeminiIngredientRequest()
    {
        User headChef = (User) seeded.get("user_gordon");
        User lineCook = (User) seeded.get("user_claire");

        LocalDate deliveryDate = LocalDate.now().plusDays(7);

        Object[][] rawData = {
            {"onions", 5.0, Unit.KG, "Løg til sauce"},
            {"løg", 2.0, Unit.KG, "Garniture"},
            {"rødløg", 1.5, Unit.KG, "Salat"},
            {"red onion", 1.0, Unit.KG, "Burger"},
            {"hvidløch", 10.0, Unit.PCS, "Massevis af hvidløg"},
            {"garlic", 5.0, Unit.PCS, "Mere hvidløg"},
            {"potatoes", 20.0, Unit.KG, "Mos"},
            {"nye kartofler", 5.0, Unit.KG, "Side dish"}
        };

        for (Object[] row : rawData) {
            String name = (String) row[0];
            double quantity = (Double) row[1];
            Unit unit = (Unit) row[2];
            String note = (String) row[3];

            IngredientRequest req = new IngredientRequest(
                name,
                quantity,
                unit,
                "Inco",
                note,
                RequestType.GENERAL_STOCK,
                deliveryDate,
                null,
                lineCook
            );

            req.approve(headChef);
            ingredientRequestDAO.create(req);
        }
    }



    private void populateWeeklyMenus() {
        Station hotStation = (Station) seeded.get("station_hot");
        Station coldStation = (Station) seeded.get("station_cold");

        DishSuggestion salmon = (DishSuggestion) seeded.get("dish_salmon");
        DishSuggestion steak = (DishSuggestion) seeded.get("dish_steak");
        DishSuggestion tartelet = (DishSuggestion) seeded.get("dish_tartelet");
        DishSuggestion roastbeef = (DishSuggestion) seeded.get("dish_roastbeef");

        WeeklyMenu menu1 = new WeeklyMenu(7, 2025);

        WeeklyMenuSlot slot1 = new WeeklyMenuSlot(DayOfWeek.MONDAY, salmon, coldStation);
        WeeklyMenuSlot slot2 = new WeeklyMenuSlot(DayOfWeek.MONDAY, steak, hotStation);
        WeeklyMenuSlot slot3 = new WeeklyMenuSlot(DayOfWeek.MONDAY, roastbeef, coldStation);
        WeeklyMenuSlot slot4 = new WeeklyMenuSlot(DayOfWeek.MONDAY, tartelet, hotStation);
        WeeklyMenuSlot slot5 = new WeeklyMenuSlot(DayOfWeek.TUESDAY, null, hotStation);

        menu1.addMenuSlot(slot1);
        menu1.addMenuSlot(slot2);
        menu1.addMenuSlot(slot3);
        menu1.addMenuSlot(slot4);
        menu1.addMenuSlot(slot5);

        menuDAO.create(menu1);

        seeded.put("menu_week7", menu1);
    }

    private void populateShoppingLists()
    {
        User claire = (User) seeded.get("user_claire");

        ShoppingList list1 = new ShoppingList(LocalDate.now().plusDays(3), claire);

        ShoppingListItem item1 = new ShoppingListItem("Frisk Dild", 15.0, Unit.BUNCH, "Grønttorvet", "Til fiskefrikadeller og garniture");
        ShoppingListItem item2 = new ShoppingListItem("Laks", 5.0, Unit.SIDES, "Hvide Sande Fiskehus", "Til rygning");

        list1.addItem(item1);
        list1.addItem(item2);

        shoppingListDAO.create(list1);

        seeded.put("shopping_list_1", list1);
    }
}
