package app.persistence.daos.impl;

import app.dtos.menu.RecentMenuDishDTO;
import app.dtos.menu.WeeklyMenuOverviewDTO;
import app.enums.MenuStatus;
import app.exceptions.DatabaseException;
import app.persistence.daos.interfaces.IWeeklyMenuDAO;
import app.persistence.entities.WeeklyMenu;
import app.utils.DBValidator;
import app.utils.TransactionUtil;
import app.utils.ValidationUtil;
import jakarta.persistence.*;

import java.util.List;
import java.util.Optional;

public class WeeklyMenuDAO implements IWeeklyMenuDAO
{
    private final EntityManagerFactory emf;

    public WeeklyMenuDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public List<WeeklyMenuOverviewDTO> findByFilter(MenuStatus status, Integer year, Integer week)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT new app.dtos.menu.WeeklyMenuOverviewDTO(" +
                            "wm.id, wm.weekNumber, wm.year, wm.menuStatus, " +
                            "CAST((SELECT COUNT(slot) FROM WeeklyMenuSlot slot WHERE slot.weeklyMenu = wm) AS long), " +
                            "wm.publishedAt) " +
                            "FROM WeeklyMenu wm " +
                            "WHERE (:status IS NULL OR wm.menuStatus = :status) " +
                            "AND   (:year   IS NULL OR wm.year = :year) " +
                            "AND   (:week   IS NULL OR wm.weekNumber = :week) " +
                            "ORDER BY wm.year DESC, wm.weekNumber ASC",
                        WeeklyMenuOverviewDTO.class)
                    .setParameter("status", status)
                    .setParameter("year", year)
                    .setParameter("week", week)
                    .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch menu overviews", e);
            }
        }
    }

    @Override
    public Optional<WeeklyMenu> findByWeekAndYear(int weekNumber, int year, MenuStatus status)
    {
        ValidationUtil.validateRange(weekNumber, 1, 53, "Week number");
        ValidationUtil.validateRange(year, 2000, 2100, "Year");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                WeeklyMenu weeklyMenu = em.createQuery(
                        "SELECT DISTINCT wm FROM WeeklyMenu wm " +
                            "LEFT JOIN FETCH wm.weeklyMenuSlots s " +
                            "LEFT JOIN FETCH s.station " +
                            "LEFT JOIN FETCH s.dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "WHERE (:status IS NULL OR wm.menuStatus = :status) " +
                            "AND   (:year   IS NULL OR wm.year = :year) " +
                            "AND   (:week   IS NULL OR wm.weekNumber = :week) ", WeeklyMenu.class)
                    .setParameter("status", status)
                    .setParameter("year", year)
                    .setParameter("week", weekNumber)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                return Optional.ofNullable(weeklyMenu);
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch menu with week: " + weekNumber + " and year: " + year, e);
            }
        }
    }

    @Override
    public List<RecentMenuDishDTO> findRecentPublishedMenuDishesByStation(Long stationId, int year, int fromWeek, int toWeek)
    {
        ValidationUtil.validateId(stationId);
        ValidationUtil.validateRange(year, 2000, 2100, "Year");
        ValidationUtil.validateRange(fromWeek, 1, 53, "From week");
        ValidationUtil.validateRange(toWeek, 1, 53, "To week");

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery(
                        "SELECT DISTINCT new app.dtos.menu.RecentMenuDishDTO(d.nameDA, d.descriptionDA) " +
                            "FROM WeeklyMenu wm " +
                            "JOIN wm.weeklyMenuSlots s " +
                            "JOIN s.dish d " +
                            "WHERE wm.menuStatus = :published " +
                            "AND s.station.id = :stationId " +
                            "AND wm.year = :year " +
                            "AND wm.weekNumber BETWEEN :fromWeek AND :toWeek ",
                        RecentMenuDishDTO.class)
                    .setParameter("published", MenuStatus.PUBLISHED)
                    .setParameter("stationId", stationId)
                    .setParameter("year", year)
                    .setParameter("fromWeek", fromWeek)
                    .setParameter("toWeek", toWeek)
                    .setMaxResults(20)
                    .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch recent station dish context", e);
            }
        }
    }

    @Override
    public WeeklyMenu getByID(Long id)
    {
        ValidationUtil.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                WeeklyMenu menu = em.createQuery(
                        "SELECT DISTINCT wm FROM WeeklyMenu wm " +
                            "LEFT JOIN FETCH wm.weeklyMenuSlots s " +
                            "LEFT JOIN FETCH s.station " +
                            "LEFT JOIN FETCH s.dish d " +
                            "LEFT JOIN FETCH d.allergens " +
                            "WHERE wm.id = :id", WeeklyMenu.class)
                    .setParameter("id", id)
                    .getSingleResult();

                return DBValidator.validateExists(menu, id, WeeklyMenu.class);
            }
            catch (NoResultException e)
            {
                throw new EntityNotFoundException("WeeklyMenu with id " + id + " not found");
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch menu with id " + id, e);
            }
        }
    }

    @Override
    public WeeklyMenu create(WeeklyMenu weeklyMenu)
    {
        ValidationUtil.validateNotNull(weeklyMenu, "Weekly Menu");
        ValidationUtil.validateRange(weeklyMenu.getWeekNumber(), 1, 53, "Week number");
        ValidationUtil.validateRange(weeklyMenu.getYear(), 2000, 2100, "Year");

        try(EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(weeklyMenu);
                em.getTransaction().commit();
                return getByID(weeklyMenu.getId());
            }
            catch
            (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to create week menu", e);
            }
        }
    }

    @Override
    public WeeklyMenu update(WeeklyMenu weeklyMenu)
    {
        ValidationUtil.validateNotNull(weeklyMenu, "WeeklyMenu");
        ValidationUtil.validateId(weeklyMenu.getId());

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                WeeklyMenu exist = em.find(WeeklyMenu.class, weeklyMenu.getId());
                DBValidator.validateExists(exist, weeklyMenu.getId(), WeeklyMenu.class);
                WeeklyMenu merged = em.merge(weeklyMenu);
                em.getTransaction().commit();
                return getByID(merged.getId());
            }
            catch (EntityNotFoundException e)
            {
                TransactionUtil.rollback(em);
                throw e;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to update week menu: " + weeklyMenu.getId(), e);
            }
        }
    }

    @Override
    public boolean delete(Long id)
    {
        ValidationUtil.validateId(id);

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                WeeklyMenu managed = em.find(WeeklyMenu.class, id);
                DBValidator.validateExists(managed, id, WeeklyMenu.class);
                em.remove(managed);
                em.getTransaction().commit();
                return true;
            }
            catch (EntityNotFoundException e)
            {
                TransactionUtil.rollback(em);
                throw e;
            }
            catch (PersistenceException e)
            {
                TransactionUtil.rollback(em);
                throw new DatabaseException("Failed to delete week menu: " + id, e);
            }
        }
    }
}
