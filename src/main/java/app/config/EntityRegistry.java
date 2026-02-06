package app.config;

import app.entities.Employee;
import app.entities.Point;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(Point.class);
        configuration.addAnnotatedClass(Employee.class);
    }
}