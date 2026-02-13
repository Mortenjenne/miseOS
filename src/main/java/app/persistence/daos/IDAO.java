package app.persistence.daos;

import java.util.Set;

public interface IDAO <T>{
    T create(T t);
    Set<T> getAll();
    T getByID(Long id);
    T update(T t);
    boolean delete(T t);
}
