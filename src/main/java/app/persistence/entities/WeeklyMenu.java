package app.persistence.entities;

import app.enums.MenuStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
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

    @Setter
    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Setter
    @Column(name = "year", nullable = false)
    private int year;

    @Setter
    @Column(name = "menu_status", nullable = false)
    private MenuStatus menuStatus;

    @OneToMany(mappedBy = "weeklyMenu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WeeklyMenuSlot> weeklyMenuSlots = new HashSet<>();

    public WeeklyMenu(int weekNumber, int year, MenuStatus menuStatus)
    {
        this.weekNumber = weekNumber;
        this.year = year;
        this.menuStatus = menuStatus;
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

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyMenu that = (WeeklyMenu) o;
        return weekNumber == that.weekNumber && year == that.year && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, weekNumber, year);
    }
}
