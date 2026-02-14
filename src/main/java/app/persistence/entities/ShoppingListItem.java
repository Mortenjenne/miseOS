package app.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "shopping_list_item")
public class ShoppingListItem implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Setter
    @Column(name = "total_quantity", nullable = false)
    private double quantity;

    @Setter
    @Column(name = "unit", nullable = false)
    private String unit;

    @Setter
    @Column(name = "supplier")
    private String supplier;

    @Setter
    @Column(name = "is_ordered")
    private boolean isOrdered;

    @Setter
    @Column(name = "notes")
    private String notes;

    @Setter
    @ManyToOne
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    public ShoppingListItem(String ingredientName, double quantity, String unit, String supplier, String notes)
    {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.supplier = supplier;
        this.notes = notes;
        this.isOrdered = false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShoppingListItem that = (ShoppingListItem) o;
        return Objects.equals(ingredientName, that.ingredientName) && Objects.equals(shoppingList, that.shoppingList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ingredientName, shoppingList);
    }
}
