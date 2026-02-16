package app.persistence.daos;

import app.persistence.entities.IEntity;

import java.util.Set;

public interface IEntityDAO<T extends IEntity, I>{
    T create(T t);
    Set<T> getAll();
    T getByID(I id);
    T update(T t);
    boolean delete(I id);
}
