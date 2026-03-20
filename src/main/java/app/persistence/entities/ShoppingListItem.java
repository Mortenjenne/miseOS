package app.persistence.entities;

import app.enums.Unit;
import app.exceptions.ConflictException;
import app.exceptions.ValidationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "shopping_list_item")
public class ShoppingListItem implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", nullable = false, length = 80)
    private String ingredientName;

    @Column(name = "total_quantity", nullable = false)
    private double quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Unit unit;

    @Column(name = "supplier", length = 80)
    private String supplier;

    @Column(name = "is_ordered")
    private boolean isOrdered;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    public ShoppingListItem(String ingredientName, double quantity, Unit unit, String supplier, String notes)
    {
        requireNotBlank(ingredientName);
        requirePositive(quantity);
        requireNotNull(unit);

        this.ingredientName = ingredientName.trim();
        this.quantity = quantity;
        this.unit = unit;
        this.supplier = supplier != null ? supplier.trim() : null;
        this.notes = notes;
        this.isOrdered = false;
    }

    @PrePersist
    void createdAt()
    {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    void setShoppingList(ShoppingList shoppingList)
    {
        this.shoppingList = shoppingList;
    }

    public void markAsOrdered()
    {
        if (this.isOrdered)
        {
            throw new ConflictException("Item is already marked as ordered");
        }
        this.isOrdered = true;
    }

    public void unmarkAsOrdered()
    {
        if (!this.isOrdered)
        {
            throw new ConflictException("Item is not marked as ordered");
        }
        this.isOrdered = false;
    }

    public void update(Double quantity, Unit unit, String supplier)
    {
        requireDraftStatus();

        if (quantity != null && quantity > 0)
        {
            this.quantity = quantity;
        }
        if (unit != null)
        {
            this.unit = unit;
        }
        if (supplier != null && !supplier.isBlank()) {
            this.supplier = supplier;
        }
    }

    private void requireNotBlank(String value)
    {
        if (value == null || value.isBlank())
        {
            throw new ValidationException("Ingredient name" + " is required");
        }
    }

    private void requirePositive(double value)
    {
        if (value <= 0)
        {
            throw new ValidationException("Quantity" + " must be greater than 0");
        }
    }

    private void requireNotNull(Object value)
    {
        if (value == null)
        {
            throw new ValidationException("Unit" + " is required");
        }
    }

    private void requireDraftStatus()
    {
        if (shoppingList != null && !shoppingList.isDraft())
        {
            throw new ConflictException("Cannot modify items in finalized list");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ShoppingListItem)) return false;
        ShoppingListItem other = (ShoppingListItem) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
