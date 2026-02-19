package app.persistence.daos;

public interface IEntityReader<T, I>
{
    T getByID(I id);
}
