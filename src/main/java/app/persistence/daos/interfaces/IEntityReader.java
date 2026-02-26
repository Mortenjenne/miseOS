package app.persistence.daos.interfaces;

public interface IEntityReader<T, I>
{
    T getByID(I id);
}
