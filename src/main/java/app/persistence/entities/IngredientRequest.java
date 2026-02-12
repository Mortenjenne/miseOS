package app.persistence.entities;

import app.enums.RequestType;
import app.enums.Status;
import app.enums.UserRole;
import app.exceptions.UnauthorizedActionException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "ingredient_request")
public class IngredientRequest implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Column(name = "unit", nullable = false)
    private double unit;

    @Setter
    @Column(name = "preferred_supplier")
    private String preferredSupplier;

    @Setter
    @Column(name = "note")
    private String note;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status requestStatus;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Setter
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "dish_suggestion_id")
    private DishSuggestion dishSuggestion;

    public IngredientRequest(String name, double unit, String preferredSupplier, String note, Status status, RequestType requestType, LocalDate deliveryDate, DishSuggestion dishSuggestion, User createdBy)
    {
        this.name = name;
        this.unit = unit;
        this.preferredSupplier = preferredSupplier;
        this.note = note;
        this.requestStatus = status;
        this.requestType = requestType;
        this.deliveryDate = deliveryDate;
        this.dishSuggestion = dishSuggestion;
        this.createdBy = createdBy;
    }

    public void approve(User headChef)
    {
        validateHeadChef(headChef);
        valideIngredientRequest();
        this.requestStatus = Status.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    @PrePersist
    public void createdAt()
    {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        IngredientRequest that = (IngredientRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

    private void valideIngredientRequest()
    {
        if (this.requestStatus != Status.PENDING)
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
