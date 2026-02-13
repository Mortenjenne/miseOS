package app.persistence.entities;

import app.enums.ShoppingListStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "shopping_list")
public class ShoppingList implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Setter
    @Column(name = "shopping_list_status", nullable = false)
    private ShoppingListStatus shoppingListStatus;

    @Setter
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ShoppingListItem> shoppingListItems = new HashSet<>();

    public ShoppingList(LocalDate deliveryDate, ShoppingListStatus shoppingListStatus, User createdBy)
    {
        this.deliveryDate = deliveryDate;
        this.shoppingListStatus = shoppingListStatus;
        this.createdBy = createdBy;
    }

    public void addItem(ShoppingListItem shoppingListItem)
    {
        if(shoppingListItem != null)
        {
            shoppingListItems.add(shoppingListItem);
            shoppingListItem.setShoppingList(this);
        }
    }

    public void removeItem(ShoppingListItem shoppingListItem)
    {
        if(shoppingListItem != null)
        {
            shoppingListItems.remove(shoppingListItem);
            shoppingListItem.setShoppingList(null);
        }
    }

    public void finalizeShoppingList()
    {
        if (this.shoppingListStatus != ShoppingListStatus.DRAFT) {
            throw new IllegalStateException("Can only finalize draft lists");
        }
        this.shoppingListStatus = ShoppingListStatus.FINALIZED;
        this.finalizedAt = LocalDateTime.now();
    }

    @PrePersist
    private void createdAt()
    {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingList that = (ShoppingList) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }
}
