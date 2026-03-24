package app.persistence.entities;

import app.enums.ShoppingListStatus;
import app.exceptions.ConflictException;
import app.exceptions.UnauthorizedActionException;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "shopping_list")
public class ShoppingList implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "shopping_list_status", nullable = false)
    private ShoppingListStatus shoppingListStatus;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ShoppingListItem> shoppingListItems = new HashSet<>();

    public ShoppingList(LocalDate deliveryDate, User createdBy)
    {
        ValidationUtil.validateNotNull(deliveryDate, "Delivery date");
        ValidationUtil.validateNotNull(createdBy, "Created by user");

        this.deliveryDate = deliveryDate;
        this.createdBy = createdBy;
        this.shoppingListStatus = ShoppingListStatus.DRAFT;
    }

    public void addItem(ShoppingListItem shoppingListItem)
    {
        ValidationUtil.validateNotNull(shoppingListItem, "Shopping list item");
        ensureDraft("add items");

        shoppingListItems.add(shoppingListItem);
        shoppingListItem.setShoppingList(this);

    }

    public void removeItem(ShoppingListItem shoppingListItem)
    {
        ValidationUtil.validateNotNull(shoppingListItem, "Shopping list item");
        ensureDraft("remove items");

        shoppingListItems.remove(shoppingListItem);
        shoppingListItem.setShoppingList(null);

    }

    public void finalizeShoppingList()
    {
        ensureDraft("finalize");

        if (!allItemsOrdered())
        {
            throw new ConflictException("Cannot finalize — not all items have been ordered");
        }

        this.shoppingListStatus = ShoppingListStatus.FINALIZED;
        this.finalizedAt = LocalDateTime.now();
    }

    public void updateDeliveryDate(LocalDate newDate)
    {
        ValidationUtil.validateNotNull(newDate, "Delivery date");
        ensureDraft("update delivery date");

        this.deliveryDate = newDate;
    }

    public void delete(User user)
    {
        if (!user.isHeadChef() && !user.isSousChef())
        {
            throw new UnauthorizedActionException("Only head or sous chef can delete shopping lists");
        }

        if (isFinalized())
        {
            throw new ConflictException("Cannot delete a finalized shopping list");
        }
    }

    @PrePersist
    private void createdAt()
    {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isDraft()
    {
        return this.shoppingListStatus == ShoppingListStatus.DRAFT;
    }

    public boolean isFinalized()
    {
        return this.shoppingListStatus == ShoppingListStatus.FINALIZED;
    }

    public int getItemCount()
    {
        return shoppingListItems.size();
    }

    public boolean allItemsOrdered()
    {
        if (shoppingListItems.isEmpty())
        {
            return false;
        }
        return shoppingListItems.stream().allMatch(ShoppingListItem::isOrdered);
    }

    private void ensureDraft(String action)
    {
        if (this.shoppingListStatus != ShoppingListStatus.DRAFT)
        {
            throw new ConflictException("Cannot " + action + " - list is " + shoppingListStatus);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ShoppingList)) return false;
        ShoppingList other = (ShoppingList) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
