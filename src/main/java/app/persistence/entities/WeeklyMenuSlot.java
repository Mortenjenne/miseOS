package app.persistence.entities;

import app.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "weekly_menu_slot")
public class WeeklyMenuSlot implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Setter
    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Setter
    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Setter
    @ManyToOne
    @JoinColumn(name = "weekly_menu_id")
    private WeeklyMenu weeklyMenu;

    @Setter
    @Column(name = "is_empty", nullable = false)
    private boolean isEmpty;

    public WeeklyMenuSlot(DayOfWeek dayOfWeek, Dish dish, Station station)
    {
        this.dayOfWeek = dayOfWeek;
        this.dish = dish;
        this.station = station;
        this.isEmpty = (dish == null);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof WeeklyMenuSlot)) return false;
        WeeklyMenuSlot other = (WeeklyMenuSlot) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

}
