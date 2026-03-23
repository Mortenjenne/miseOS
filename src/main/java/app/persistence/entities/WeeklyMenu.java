package app.persistence.entities;

import app.enums.MenuStatus;
import app.exceptions.ConflictException;
import app.exceptions.UnauthorizedActionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "weekly_menu")
public class WeeklyMenu implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(name = "year", nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_status", nullable = false)
    private MenuStatus menuStatus;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne
    @JoinColumn(name = "published_by_user_id")
    private User publishedBy;

    @OneToMany(mappedBy = "weeklyMenu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WeeklyMenuSlot> weeklyMenuSlots = new HashSet<>();

    public WeeklyMenu(int weekNumber, int year)
    {
        this.weekNumber = weekNumber;
        this.year = year;
        this.menuStatus = MenuStatus.DRAFT;
    }

    public void publish(User publisher)
    {
        if (!publisher.isHeadChef() && !publisher.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chefs or souschefs can publish menus");
        }

        if (this.menuStatus == MenuStatus.PUBLISHED)
        {
            throw new ConflictException("Menu is already published");
        }

        this.menuStatus = MenuStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.publishedBy = publisher;
    }

    public void addMenuSlot(WeeklyMenuSlot weeklyMenuSlot)
    {
        if(weeklyMenuSlot != null)
        {
            weeklyMenuSlots.add(weeklyMenuSlot);
            weeklyMenuSlot.setWeeklyMenu(this);
        }
    }

    public void removeMenuSlot(WeeklyMenuSlot weeklyMenuSlot)
    {
        if(weeklyMenuSlot != null)
        {
            weeklyMenuSlots.remove(weeklyMenuSlot);
            weeklyMenuSlot.setWeeklyMenu(null);
        }
    }

    public void delete(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chefs or sous chefs can delete menus");
        }

        if (this.menuStatus == MenuStatus.PUBLISHED)
        {
            throw new ConflictException("Cannot delete a published menu — archive it instead");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WeeklyMenu)) return false;
        WeeklyMenu other = (WeeklyMenu) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
