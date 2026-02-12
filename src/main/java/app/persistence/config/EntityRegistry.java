package app.persistence.config;

import app.persistence.entities.User;
import org.hibernate.cfg.Configuration;

final class EntityRegistry
{

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration)
    {
        configuration.addAnnotatedClass(User.class);
    }
}
