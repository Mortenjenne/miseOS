package app.config;

import app.persistence.entities.*;
import org.hibernate.cfg.Configuration;

final class EntityRegistry
{

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration)
    {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Allergen.class);
        configuration.addAnnotatedClass(DishSuggestion.class);
        configuration.addAnnotatedClass(IngredientRequest.class);
        configuration.addAnnotatedClass(ShoppingList.class);
        configuration.addAnnotatedClass(ShoppingListItem.class);
        configuration.addAnnotatedClass(Station.class);
        configuration.addAnnotatedClass(WeeklyMenu.class);
        configuration.addAnnotatedClass(WeeklyMenuSlot.class);
    }
}
