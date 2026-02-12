package app.persistence.entities;

import app.enums.Status;
import app.enums.UserRole;
import app.exceptions.UnauthorizedActionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String nameDA;

    private String nameEN;

    private String descriptionDA;

    private String descriptionEN;

    private Status dishStatus;

    private int weekNumber;

    private int year;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @ManyToMany
    @JoinTable(name = "dish_allergen", joinColumns = @JoinColumn(name = "dish_id"), inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    private Set<Allergen> allergens = new HashSet<>();

    @PrePersist
    private void createdAt()
    {
        this.createdAt = LocalDateTime.now();
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

    public void approve(User headChef)
    {
        validateHeadChef(headChef);
        validateDishStatus();
        this.dishStatus = Status.APPROVED;
        this.reviewedBy = headChef;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User headChef)
    {
        validateHeadChef(headChef);
        validateDishStatus();
        this.dishStatus = Status.REJECTED;
        this.reviewedBy = headChef;
        this.reviewedAt = LocalDateTime.now();
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

        if(currentUser.getUserRole() != UserRole.HEAD_CHEF)
        {
            throw new UnauthorizedActionException("Only head chefs can approve dish suggestions");
        }
    }

}
