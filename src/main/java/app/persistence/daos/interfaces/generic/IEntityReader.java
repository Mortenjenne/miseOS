package app.persistence.daos.interfaces.generic;

public interface IEntityReader<T, I>
{
    T getByID(I id);
}
