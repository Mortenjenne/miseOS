package app.persistence.entities;

import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import app.exceptions.ValidationException;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "dish_suggestion")
public class DishSuggestion implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name_da", nullable = false)
    private String nameDA;

    @Setter
    @Column(name = "name_en")
    private String nameEN;

    @Setter
    @Column(name = "description_da", nullable = false)
    private String descriptionDA;

    @Setter
    @Column(name = "description_en")
    private String descriptionEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "dish_status", nullable = false)
    private Status dishStatus;

    @Setter
    @Column(name = "feedback")
    private String feedback;

    @Setter
    @Column(name = "target_year")
    private Integer targetYear;

    @Setter
    @Column(name = "target_week")
    private Integer targetWeek;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @ManyToMany
    @JoinTable(name = "dish_allergen", joinColumns = @JoinColumn(name = "dish_suggestion_id"), inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    private Set<Allergen> allergens = new HashSet<>();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DishSuggestion(String nameDA, String descriptionDA, Integer targetWeek, Integer targetYear, Station station, User createdBy, Set<Allergen> allergens)
    {
        ValidationUtil.validateNotBlank(nameDA, "Name DA");
        ValidationUtil.validateNotNull(descriptionDA, "Description DA");
        ValidationUtil.validateNotNull(station, "Station");
        ValidationUtil.validateNotNull(createdBy, "User created by");
        validateWeek(targetWeek);
        validateYear(targetYear);

        this.nameDA = nameDA;
        this.descriptionDA = descriptionDA;
        this.targetWeek = targetWeek;
        this.targetYear = targetYear;
        this.station = station;
        this.createdBy = createdBy;
        this.allergens = allergens;
        this.dishStatus = Status.PENDING;
    }

    public void updateContent(String newNameDA, String newNameEN, String newDescriptionDA, String newDescriptionEN, Set<Allergen> newAllergens, User editor)
    {
        if (newNameDA != null && !newNameDA.isBlank())
        {
            this.nameDA = newNameDA.trim();
        }
        if (newDescriptionDA != null && !newDescriptionDA.isBlank())
        {
            this.descriptionDA = newDescriptionDA.trim();
        }

        this.nameEN = newNameEN != null ? newNameEN.trim() : this.nameEN;
        this.descriptionEN = newDescriptionEN != null ? newDescriptionEN.trim() : this.descriptionEN;

        if (newAllergens != null)
        {
            this.allergens.clear();
            this.allergens.addAll(newAllergens);
        }
    }

    public String getName(String language)
    {
        return "da".equalsIgnoreCase(language) ? nameDA : nameEN;
    }

    public String getDescription(String language) {return "da".equalsIgnoreCase(language) ? descriptionDA : descriptionEN;}

    public void approve(User approver)
    {
        validateApprover(approver);
        ensurePending();
        this.dishStatus = Status.APPROVED;
        this.reviewedBy = approver;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User approver, String feedback)
    {
        validateApprover(approver);
        ensurePending();
        this.dishStatus = Status.REJECTED;
        this.reviewedBy = approver;
        this.reviewedAt = LocalDateTime.now();
        this.feedback = feedback.trim();
    }

    public void addAllergen(Allergen allergen)
    {
        if(allergen != null)
        {
            this.allergens.add(allergen);
        }
    }

    public void removeAllergen(Allergen allergen)
    {
        if(allergen != null)
        {
            this.allergens.remove(allergen);
        }

    }

    public LocalDate getDeadlineDate()
    {
        ValidationUtil.validateNotNull(targetWeek, "Target week");
        ValidationUtil.validateNotNull(targetYear, "Target year");

        LocalDate targetMonday = LocalDate.of(targetYear, 1, 1)
            .with(WeekFields.ISO.weekOfYear(), targetWeek)
            .with(WeekFields.ISO.dayOfWeek(), 1);

        return targetMonday.minusDays(4);
    }

    public void checkCreationAllowed(LocalDate today)
    {
        if (!today.isBefore(getDeadlineDate()))
        {
            throw new IllegalStateException("Deadline passed. Last chance was " + getDeadlineDate());
        }
    }

    public boolean isPastDeadline(LocalDate today)
    {
        LocalDate deadline = getDeadlineDate();

        return !today.isBefore(deadline);
    }

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    private void ensurePending()
    {
        if (this.dishStatus != Status.PENDING)
        {
            throw new IllegalStateException("Only pending suggestions allowed here");
        }
    }

    private void validateApprover(User approver)
    {
        if(approver == null)
        {
            throw new IllegalArgumentException("Approver is required");
        }

        if(!approver.isHeadChef() && !approver.isSousChef())
        {
            throw new UnauthorizedActionException("Only head chef or sous chef can approve / reject dish suggestions");
        }
    }

    private void validateWeek(int week)
    {
        if (week < 1 || week > 53)
        {
            throw new ValidationException("Week must be between 1 and 53");
        }
    }

    private void validateYear(int year)
    {
        if (year < 2020 || year > 2100)
        {
            throw new ValidationException("Year must be between 2020 and 2100");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof DishSuggestion)) return false;
        DishSuggestion other = (DishSuggestion) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
