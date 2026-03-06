package app.persistence.entities;

import app.enums.RequestType;
import app.enums.Status;
import app.enums.Unit;
import app.exceptions.UnauthorizedActionException;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Column(name = "quantity", nullable = false)
    private double quantity;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Unit unit;

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

    @Setter
    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    public IngredientRequest(String name, double quantity, Unit unit, String preferredSupplier, String note, RequestType requestType, LocalDate deliveryDate, Dish dish, User createdBy)
    {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.preferredSupplier = preferredSupplier;
        this.note = note;
        this.requestStatus = Status.PENDING;
        this.requestType = requestType;
        this.deliveryDate = deliveryDate;
        this.dish = dish;
        this.createdBy = createdBy;
    }

    public void approve(User headChef)
    {
        requireHeadOrSousChef(headChef);
        valideIngredientRequest();
        this.requestStatus = Status.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User headChef)
    {
        requireHeadOrSousChef(headChef);
        valideIngredientRequest();
        this.requestStatus = Status.REJECTED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void update(String name, double quantity, Unit unit, String preferredSupplier, String note, LocalDate deliveryDate, Dish dish) {
        requirePendingStatus();
        ValidationUtil.validateNotBlank(name, "Name");
        ValidationUtil.validatePositive(quantity, "Quantity");
        ValidationUtil.validateNotNull(unit, "Unit");
        ValidationUtil.validateNotNull(deliveryDate, "Delivery date");

        this.name = name.trim();
        this.quantity = quantity;
        this.unit = unit;
        this.preferredSupplier = preferredSupplier;
        this.note = note;
        this.deliveryDate = deliveryDate;
        this.dish = dish;
    }

    public boolean isPending() {
        return this.requestStatus == Status.PENDING;
    }

    @PrePersist
    public void createdAt()
    {
        this.createdAt = LocalDateTime.now();
    }


    private void valideIngredientRequest()
    {
        if (this.requestStatus != Status.PENDING)
        {
            throw new IllegalStateException("Only pending suggestions allowed here");
        }
    }

    private void requirePendingStatus()
    {
        if (this.requestStatus != Status.PENDING)
        {
            throw new IllegalStateException("Can only modify pending requests. Current: " + requestStatus);
        }
    }

    private void requireHeadOrSousChef(User currentUser)
    {
        if(currentUser == null)
        {
            throw new IllegalArgumentException("Head chef cannot be null");
        }

        if(!currentUser.isHeadChef() && !currentUser.isSousChef())
        {
            throw new UnauthorizedActionException("Only head or sous chefs can approve dish suggestions");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof IngredientRequest)) return false;
        IngredientRequest other = (IngredientRequest) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
