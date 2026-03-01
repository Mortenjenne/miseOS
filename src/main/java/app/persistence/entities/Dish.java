package app.persistence.entities;

import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "dish")
public class Dish implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_da", nullable = false)
    private String nameDA;

    @Column(name = "name_en")
    private String nameEN;

    @Column(name = "description_da", nullable = false)
    private String descriptionDA;

    @Column(name = "description_en")
    private String descriptionEN;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToMany
    @JoinTable(name = "dish_allergen", joinColumns = @JoinColumn(name = "dish_id"), inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    private Set<Allergen> allergens = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "origin_week", nullable = false)
    private int originWeek;

    @Column(name = "origin_year", nullable = false)
    private int originYear;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Dish(String nameDA, String descriptionDA, Station station, Set<Allergen> allergens, User createdBy, int originWeek, int originYear)
    {
        ValidationUtil.validateNotBlank(nameDA, "Name");
        ValidationUtil.validateNotBlank(descriptionDA, "Description");
        ValidationUtil.validateNotNull(station, "Station");
        ValidationUtil.validateNotNull(createdBy, "Created by");

        this.nameDA = nameDA;
        this.descriptionDA = descriptionDA;
        this.station = station;
        this.allergens = allergens != null ? allergens : new HashSet<>();
        this.createdBy = createdBy;
        this.originWeek = originWeek;
        this.originYear = originYear;
        this.isActive = true;
    }

    public void update(String nameDA, String descriptionDA, String nameEN, String descriptionEN, Set<Allergen> allergens)
    {
        ValidationUtil.validateNotBlank(nameDA, "Name Danish");
        ValidationUtil.validateNotBlank(descriptionDA, "Description Danish");

        this.nameDA = nameDA.trim();
        this.descriptionDA = descriptionDA.trim();
        this.nameEN = nameEN != null ? nameEN.trim() : null;
        this.descriptionEN = descriptionEN != null ? descriptionEN.trim() : null;

        if (allergens != null)
        {
            this.allergens.clear();
            this.allergens.addAll(allergens);
        }
    }

    public boolean hasTranslation()
    {
        return nameEN != null && !nameEN.isBlank();
    }

    public boolean isForWeek(int week, int year)
    {
        return this.originWeek == week && this.originYear == year;
    }

    public String getName(String language)
    {
        return "en".equalsIgnoreCase(language) && nameEN != null ? nameEN : nameDA;
    }

    public String getDescription(String language)
    {
        return "en".equalsIgnoreCase(language) && descriptionEN != null ? descriptionEN : descriptionDA;
    }

    public void deactivate()
    {
        this.isActive = false;
    }

    public void activate()
    {
        this.isActive = true;
    }

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Dish other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
