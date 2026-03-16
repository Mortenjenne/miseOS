package app.persistence.daos.interfaces.generic;

import app.persistence.entities.IEntity;

import java.util.Set;

public interface
IEntityDAO<T extends IEntity, I> extends IEntityReader<T, I>
{
    T create(T t);
    T update(T t);
    boolean delete(I id);
}
