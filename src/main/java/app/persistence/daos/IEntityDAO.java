package app.persistence.daos;

import java.util.Set;

public interface IEntityDAO<T, I>{
    T create(T t);
    Set<T> getAll();
    T getByID(I id);
    T update(T t);
    boolean delete(I id);
}
