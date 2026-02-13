package app.utils;

import jakarta.persistence.EntityNotFoundException;


public class DBValidator
{

    public static void validateId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new IllegalArgumentException("Invalid ID: Must be provided and greater than 0.");
        }
    }

    public static <T> T validateExists(T entity, Object id, Class<T> entityClass) {
        if (entity == null)
        {
            String className = entityClass.getSimpleName();
            throw new EntityNotFoundException(className + " with ID " + id + " was not found.");
        }
        return entity;
    }

    public static void validateNotNull(Object obj, String entityName)
    {
        if (obj == null) {
            throw new IllegalArgumentException(entityName + " cannot be null.");
        }
    }
}
