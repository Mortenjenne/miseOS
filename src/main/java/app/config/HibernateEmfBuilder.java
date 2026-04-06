package app.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public final class HibernateEmfBuilder
{
    private static final Logger logger = LoggerFactory.getLogger(HibernateEmfBuilder.class);

    private HibernateEmfBuilder() {}

    static EntityManagerFactory build(Properties props)
    {
        try
        {
            Configuration configuration = new Configuration();
            configuration.setProperties(props);

            EntityRegistry.registerEntities(configuration);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
            return sf.unwrap(EntityManagerFactory.class);

        }
        catch (Throwable ex)
        {
            logger.error("Initial SessionFactory creation failed: {}", ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }
}
