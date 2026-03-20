package app.persistence.entities;

import app.enums.RequestType;
import app.enums.Status;
import app.enums.Unit;
import app.exceptions.ConflictException;
import app.exceptions.UnauthorizedActionException;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ingredient_request")
public class IngredientRequest implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "quantity", nullable = false)
    private double quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Unit unit;

    @Column(name = "preferred_supplier", length = 100)
    private String preferredSupplier;

    @Column(name = "note")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status requestStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    public IngredientRequest(String name, double quantity, Unit unit, String preferredSupplier, String note, RequestType requestType, LocalDate deliveryDate, Dish dish, User createdBy)
    {
        ValidationUtil.validateNotBlank(name, "Name");
        ValidationUtil.validatePositive(quantity, "Quantity");
        ValidationUtil.validateNotNull(unit, "Unit");
        ValidationUtil.validateNotNull(requestType, "Request type");
        ValidationUtil.validateNotNull(deliveryDate, "Delivery date");
        ValidationUtil.validateNotNull(createdBy, "Created by");

        if (requestType == RequestType.DISH_SPECIFIC && dish == null)
        {
            throw new IllegalArgumentException("Dish is required for dish-specific requests");
        }

        if (requestType == RequestType.GENERAL_STOCK && dish != null)
        {
            throw new IllegalArgumentException("Dish must be null for general requests");
        }

        this.name = name.trim();
        this.quantity = quantity;
        this.unit = unit;
        this.preferredSupplier = preferredSupplier;
        this.note = note != null ? note.trim() : null;
        this.requestStatus = Status.PENDING;
        this.requestType = requestType;
        this.deliveryDate = deliveryDate;
        this.dish = dish;
        this.createdBy = createdBy;
    }

    public void approve(User headChef)
    {
        requireHeadOrSousChef(headChef);
        requirePendingStatus();
        this.requestStatus = Status.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(User headChef)
    {
        requireHeadOrSousChef(headChef);
        requirePendingStatus();
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

    public void delete(User user)
    {
        boolean isOwner = this.createdBy.getId().equals(user.getId());
        boolean isHeadChefOrSousChef = user.isHeadChef() || user.isSousChef();

        if (!isOwner && !isHeadChefOrSousChef)
        {
            throw new UnauthorizedActionException("Only owner, head chef, or sous chef can delete requests");
        }

        if (!isPending())
        {
            throw new ConflictException("Cannot delete a non-pending request");
        }
    }

    public void adjustQuantityForApproval(Double quantity, String note)
    {
        requirePendingStatus();

        if (quantity != null)
        {
            ValidationUtil.validatePositive(quantity, "Quantity");
            this.quantity = quantity;
        }

        if (note != null)
        {
            this.note = note.trim();
        }
    }

    public boolean isPending() {
        return this.requestStatus == Status.PENDING;
    }

    @PrePersist
    private void createdAt()
    {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate(){this.updatedAt = LocalDateTime.now();}

    private void requirePendingStatus()
    {
        if (this.requestStatus != Status.PENDING)
        {
            throw new ConflictException("Can only modify pending requests. Current: " + requestStatus);
        }
    }

    private void requireHeadOrSousChef(User currentUser)
    {
        if(currentUser == null)
        {
            throw new IllegalArgumentException("User cannot be null");
        }

        if(!currentUser.isHeadChef() && !currentUser.isSousChef())
        {
            throw new UnauthorizedActionException("Only head or sous chefs can approve ingredient requests");
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
