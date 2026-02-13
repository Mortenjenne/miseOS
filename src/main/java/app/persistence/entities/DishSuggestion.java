package app.persistence.entities;

import app.enums.Status;
import app.exceptions.UnauthorizedActionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
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


    public String getName(String language)
    {
        return "da".equalsIgnoreCase(language) ? nameDA : nameEN;
    }

    public String getDescription(String language)
    {
        return "da".equalsIgnoreCase(language) ? descriptionDA : descriptionEN;
    }

    public void approve(User headChef)
    {
        validateHeadChef(headChef);
        validateDishStatus();
        this.dishStatus = Status.APPROVED;
        this.reviewedBy = headChef;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User headChef, String feedback)
    {
        validateHeadChef(headChef);
        validateDishStatus();
        this.dishStatus = Status.REJECTED;
        this.reviewedBy = headChef;
        this.reviewedAt = LocalDateTime.now();
        this.feedback = feedback;
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

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    private void validateDishStatus()
    {
        if (this.dishStatus != Status.PENDING)
        {
            throw new IllegalStateException("Only pending suggestions allowed here");
        }
    }

    private void validateHeadChef(User currentUser)
    {
        if(currentUser == null)
        {
            throw new IllegalArgumentException("Head chef cannot be null");
        }

        if(!currentUser.isHeadChef())
        {
            throw new UnauthorizedActionException("Only head chefs can approve dish suggestions");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        DishSuggestion that = (DishSuggestion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

}
