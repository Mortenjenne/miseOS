package app.testutils;

import app.enums.DayOfWeek;
import app.enums.RequestType;
import app.enums.Unit;
import app.enums.UserRole;
import app.persistence.daos.impl.*;
import app.persistence.daos.interfaces.*;
import app.persistence.entities.*;
import app.utils.EUAllergens;
import app.utils.PasswordUtil;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.util.*;

public class TestPopulator
{
    private final IStationDAO stationDAO;
    private final IUserDAO userDAO;
    private final IAllergenDAO allergenDAO;
    private final IDishSuggestionDAO dishSuggestionDAO;
    private final IDishDAO dishDAO;
    private final IWeeklyMenuDAO menuDAO;
    private final IIngredientRequestDAO ingredientRequestDAO;
    private final IShoppingListDAO shoppingListDAO;
    private final Map<String, IEntity> seeded;

    public TestPopulator(EntityManagerFactory emf)
    {
        this.stationDAO = new StationDAO(emf);
        this.userDAO = new UserDAO(emf);
        this.allergenDAO = new AllergenDAO(emf);
        this.dishSuggestionDAO = new DishSuggestionDAO(emf);
        this.dishDAO = new DishDAO(emf);
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
        populateDishes();
        populateIngredientRequest();
        populateWeeklyMenus();
        populateShoppingLists();
        populateForGeminiIngredientRequest();
    }

    public Map<String, IEntity> getSeededData()
    {
        return Collections.unmodifiableMap(seeded);
    }

    private void populateStations()
    {
        Station s1 = new Station("Cold Kitchen", "Salads & Starters");
        Station s2 = new Station("Hot Kitchen", "Main Courses");
        Station s3 = new Station("Pastry", "Desserts & Bread");
        Station s4 = new Station("Grill", "Steaks & BBQ");
        Station s5 = new Station("Salad", "Salads and crudites");

        stationDAO.create(s1);
        stationDAO.create(s2);
        stationDAO.create(s3);
        stationDAO.create(s4);
        stationDAO.create(s5);

        seeded.put("station_cold", s1);
        seeded.put("station_hot", s2);
        seeded.put("station_pastry", s3);
        seeded.put("station_grill", s4);
        seeded.put("station_salad", s5);
    }

    private void populateUsers()
    {
        User u1 = new User("Gordon", "Ramsay", "gordon@kitchen.com", PasswordUtil.hashPassword("Hash1"), UserRole.HEAD_CHEF);
        User u2 = new User("Claire", "Smyth", "claire@pastry.com", PasswordUtil.hashPassword("Hash2"), UserRole.LINE_COOK);
        User u3 = new User("Marco", "Pierre", "marco@grill.com", PasswordUtil.hashPassword("Hash3"), UserRole.SOUS_CHEF);
        User u4 = new User("Rene", "Redzepi", "rene@cold.com", PasswordUtil.hashPassword("Hash4"), UserRole.LINE_COOK);

        Station cold = (Station) seeded.get("station_cold");
        Station hot = (Station) seeded.get("station_hot");
        Station pastry = (Station) seeded.get("station_pastry");
        Station grill = (Station) seeded.get("station_grill");

        u1.assignToStation(cold);
        u2.assignToStation(hot);
        u3.assignToStation(pastry);
        u4.assignToStation(grill);

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

        List<Allergen> euAllergens = EUAllergens.getAll();

        euAllergens.forEach(a ->
        {
            Allergen allergen = allergenDAO.create(a);
            String allergenKey = "allergen_"+ allergen.getNameEN().toLowerCase();
            seeded.put(allergenKey, allergen);
        });
    }

    private void populateDishSuggestions()
    {
        Station hotStation = (Station) seeded.get("station_hot");
        Station coldStation = (Station) seeded.get("station_cold");

        User cookClaire = (User) seeded.get("user_claire");
        User cookMarco = (User) seeded.get("user_marco");

        Allergen gluten = (Allergen) seeded.get("allergen_gluten");
        Allergen milk = (Allergen) seeded.get("allergen_milk");
        Allergen eggs = (Allergen) seeded.get("allergen_eggs");
        Allergen fish = (Allergen) seeded.get("allergen_fish");

        Set<Allergen> allergens = new HashSet<>();
        allergens.add(gluten);
        allergens.add(milk);
        allergens.add(eggs);
        allergens.add(fish);

        DishSuggestion d1 = new DishSuggestion(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            7,
            2026,
            coldStation,
            cookClaire,
            new HashSet<>(allergens)
        );

        DishSuggestion d2 = new DishSuggestion(
            "Bøf Bearnaise",
            "Oksemørbrad med hjemmelavet bearnaise",
            7,
            2026,
            hotStation,
            cookMarco,
            new HashSet<>(allergens)
        );

        DishSuggestion d3 = new DishSuggestion(
            "Tarteletter",
            "Høns i asparges",
            7,
            2026,
            hotStation,
            cookClaire,
            new HashSet<>(allergens)
        );

        DishSuggestion d4 = new DishSuggestion(
            "Roastbeef",
            "Roastbeef med remoulade, syltet agurk og sprøde løg",
            7,
            2026,
            coldStation,
            cookMarco,
            new HashSet<>(allergens)
        );

        DishSuggestion d5 = new DishSuggestion(
            "Sushi",
            "Sashimi og syltet ingefær",
            8,
            2026,
            coldStation,
            cookClaire,
            new HashSet<>(allergens)
        );

        dishSuggestionDAO.create(d1);
        dishSuggestionDAO.create(d2);
        dishSuggestionDAO.create(d3);
        dishSuggestionDAO.create(d4);
        dishSuggestionDAO.create(d5);

        seeded.put("suggestion_salmon", d1);
        seeded.put("suggestion_steak", d2);
        seeded.put("suggestion_tartelet", d3);
        seeded.put("suggestion_roastbeef", d4);
        seeded.put("suggestion_sushi",d5);
    }

    private void populateDishes()
    {
        Station hotStation = (Station) seeded.get("station_hot");
        Station coldStation = (Station) seeded.get("station_cold");

        User gordon = (User) seeded.get("user_gordon");

        Allergen gluten = (Allergen) seeded.get("allergen_gluten");
        Allergen milk = (Allergen) seeded.get("allergen_milk");
        Allergen eggs = (Allergen) seeded.get("allergen_eggs");
        Allergen fish = (Allergen) seeded.get("allergen_fish");

        Set<Allergen> allergens = new HashSet<>(Set.of(gluten, milk, eggs, fish));

        Dish dish1 = new Dish(
            "Røget Laks",
            "Laks med dildcreme og rugbrødschips",
            coldStation,
            new HashSet<>(allergens),
            gordon,
            7,
            2026
        );

        Dish dish2 = new Dish(
            "Bøf Bearnaise",
            "Oksemørbrad med hjemmelavet bearnaise",
            hotStation,
            new HashSet<>(allergens),
            gordon,
            7,
            2026
        );

        Dish dish3 = new Dish(
            "Tarteletter",
            "Høns i asparges",
            hotStation,
            new HashSet<>(allergens),
            gordon,
            7,
            2026
        );

        Dish dish4 = new Dish(
            "Roastbeef",
            "Roastbeef med remoulade og sprøde løg",
            coldStation,
            new HashSet<>(allergens),
            gordon,
            5,
            2026
        );

        Dish dish5 = new Dish(
            "Stegt Flæsk",
            "Med persillesovs og kartofler",
            hotStation,
            new HashSet<>(allergens),
            gordon,
            5,
            2026
        );

        dish5.update(
            "Stegt Flæsk",
            "Med persillesovs og kartofler",
            "Fried Pork Belly",
            "With parsley sauce and potatoes",
            new HashSet<>(allergens)
        );

        Dish dish6 = new Dish(
            "Jeppe's kål",
            "Faseret kål med brun sovs",
            hotStation,
            new HashSet<>(),
            gordon,
            1,
            2026
        );

        Dish dish7 = new Dish(
            "Grillet Kylling",
            "Serveret med citron og timian",
            hotStation,
            new HashSet<>(allergens),
            gordon,
            10,
            2026
        );

        Dish dish8 = new Dish(
            "Caesar Salad",
            "Romainesalat med parmesan og croutoner",
            coldStation,
            new HashSet<>(allergens),
            gordon,
            10,
            2026
        );

        Dish dish9 = new Dish(
            "Chokolademousse",
            "Mørk chokolade med flødeskum",
            (Station) seeded.get("station_pastry"),
            new HashSet<>(allergens),
            gordon,
            10,
            2026
        );

        Dish dish10 = new Dish(
            "Test Slette-Ret",
            "Kun til tests, bruges ikke i menuer",
            hotStation,
            new HashSet<>(allergens),
            gordon,
            12,
            2026
        );

        dish6.deactivate(); //For inactive test

        dishDAO.create(dish1);
        dishDAO.create(dish2);
        dishDAO.create(dish3);
        dishDAO.create(dish4);
        dishDAO.create(dish5);
        dishDAO.create(dish6);
        dishDAO.create(dish7);
        dishDAO.create(dish8);
        dishDAO.create(dish9);
        dishDAO.create(dish10);

        seeded.put("dish_salmon", dish1);
        seeded.put("dish_boeuf", dish2);
        seeded.put("dish_tartelet", dish3);
        seeded.put("dish_roastbeef", dish4);
        seeded.put("dish_roasted_pork", dish5);
        seeded.put("dish_old", dish6);
        seeded.put("dish_chicken", dish7);
        seeded.put("dish_caesar", dish8);
        seeded.put("dish_mousse", dish9);
        seeded.put("dish_delete", dish10);
    }

    private void populateIngredientRequest()
    {
        User headChef = (User) seeded.get("user_gordon");
        User lineCook = (User) seeded.get("user_claire");
        Dish dish = (Dish) seeded.get("dish_salmon");

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

        List<IngredientRequest> requests = List.of(
            new IngredientRequest("onions", 5.0, Unit.KG, "Inco", "Løg til sauce", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("onio", 7.0, Unit.KG, "Inco", "Løg til sauce", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("løg", 2.0, Unit.KG, "Inco", "Garniture", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("rødløg", 1.5, Unit.KG, "Inco", "Salat", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("red onion", 1.0, Unit.KG, "Inco", "Burger", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("hvidløg", 10.0, Unit.PCS, "Inco", "Massevis af hvidløg", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("hvidløch", 10.0, Unit.PCS, "Inco", "Massevis af hvidløg", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("garlic", 5.0, Unit.PCS, "Inco", "Mere hvidløg", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("potatoes", 20.0, Unit.KG, "Inco", "Mos", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("nye kartofler", 5.0, Unit.KG, "Inco", "Side dish", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("heavy cream", 2.0,  Unit.L,"Inco", "Sauce", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("fløde 38%",1.0,  Unit.L, "Inco", "Dessert", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("mælk",3.0,  Unit.L, "Inco", "Bechamel", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("butter", 1.0,  Unit.KG, "Arla", "Sauce",RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("smør", 2.0,  Unit.KG,"Arla", "Bagning",RequestType.GENERAL_STOCK, deliveryDate, null, lineCook),
            new IngredientRequest("beurre", 0.5,  Unit.KG,"Arla", "Fransk sauce", RequestType.GENERAL_STOCK, deliveryDate, null, lineCook)
        );

        requests.forEach(req ->
        {
            req.approve(headChef);
            ingredientRequestDAO.create(req);
        });
    }

    private void populateWeeklyMenus()
    {
        Station hot = (Station) seeded.get("station_hot");
        Station cold = (Station) seeded.get("station_cold");
        Station pastry = (Station) seeded.get("station_pastry");

        User gordon = (User) seeded.get("user_gordon");

        Dish salmon = (Dish) seeded.get("dish_salmon");
        Dish steak = (Dish) seeded.get("dish_boeuf");
        Dish tartelet = (Dish) seeded.get("dish_tartelet");
        Dish roastbeef = (Dish) seeded.get("dish_roastbeef");
        Dish pork = (Dish) seeded.get("dish_roasted_pork");
        Dish chicken = (Dish) seeded.get("dish_chicken");
        Dish caesar = (Dish) seeded.get("dish_caesar");
        Dish mousse = (Dish) seeded.get("dish_mousse");
        Dish inactive = (Dish) seeded.get("dish_old");

        //Published menu
        WeeklyMenu fullMenu = new WeeklyMenu(7, 2026);

        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.MONDAY, salmon, cold));
        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.MONDAY, steak, hot));

        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.TUESDAY, roastbeef, cold));
        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.TUESDAY, pork, hot));

        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.WEDNESDAY, caesar, cold));
        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.WEDNESDAY, chicken, hot));

        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.THURSDAY, mousse, pastry));
        fullMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.THURSDAY, tartelet, hot));

        fullMenu.publish(gordon);

        menuDAO.create(fullMenu);
        seeded.put("menu_full", fullMenu);

        //Draft menu
        WeeklyMenu draftMenu = new WeeklyMenu(8, 2026);

        draftMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.MONDAY, salmon, cold));
        draftMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.TUESDAY, null, hot));

        menuDAO.create(draftMenu);
        seeded.put("menu_draft", draftMenu);

        //Inactive menu
        WeeklyMenu inactiveMenu = new WeeklyMenu(9, 2026);

        inactiveMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.WEDNESDAY, inactive, hot));

        menuDAO.create(inactiveMenu);
        seeded.put("menu_inactive", inactiveMenu);

        //Slot menu
        WeeklyMenu slotMenu = new WeeklyMenu(10, 2026);

        slotMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.MONDAY, salmon, cold));
        slotMenu.addMenuSlot(new WeeklyMenuSlot(DayOfWeek.MONDAY, steak, hot));

        menuDAO.create(slotMenu);
        seeded.put("menu_slots", slotMenu);
    }

    private void populateShoppingLists()
    {
        User claire = (User) seeded.get("user_claire");
        User gordon = (User) seeded.get("user_gordon");

        ShoppingList list1 = new ShoppingList(LocalDate.now().plusDays(3), claire);
        ShoppingListItem item1 = new ShoppingListItem("Frisk Dild", 15.0, Unit.BUNCH, "Grønttorvet", "Til fiskefrikadeller og garniture");
        ShoppingListItem item2 = new ShoppingListItem("Laks", 5.0, Unit.SIDES, "Hvide Sande Fiskehus", "Til rygning");
        list1.addItem(item1);
        list1.addItem(item2);
        shoppingListDAO.create(list1);
        seeded.put("shopping_list_draft", list1);

        ShoppingList list2 = new ShoppingList(LocalDate.now().plusDays(6), gordon);
        ShoppingListItem item3 = new ShoppingListItem("Smør", 10.0, Unit.KG, "Arla", "Usaltet");
        list2.addItem(item3);
        item3.markAsOrdered();
        list2.finalizeShoppingList();
        shoppingListDAO.create(list2);
        seeded.put("shopping_list_finalized", list2);
    }
}
