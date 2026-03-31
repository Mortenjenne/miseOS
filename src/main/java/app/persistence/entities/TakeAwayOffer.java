package app.persistence.entities;

import app.exceptions.ConflictException;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "take_away_offer")
public class TakeAwayOffer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "soldout")
    private boolean soldOut;

    @Column(name = "offered_portions")
    private int offeredPortions;

    @Column(name = "available_portions_left")
    private int availablePortions ;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    Dish dish;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TakeAwayOffer(int offeredPortions, User createdBy, Dish dish)
    {
        ValidationUtil.validatePositive(offeredPortions, "Offered portions");
        ValidationUtil.validateNotNull(createdBy, "User");
        ValidationUtil.validateNotNull(dish, "Dish");

        this.enabled = true;
        this.offeredPortions = offeredPortions;
        this.createdBy = createdBy;
        this.dish = dish;
    }

    public void sellPortions(int quantity)
    {
        ValidationUtil.validatePositive(quantity, "Quantity");
        requireNotSoldOut();

        int remainingPortions = quantity - availablePortions;

        if(remainingPortions < 0)
        {
            throw new ConflictException("Not enough portions left for your order");
        }

        if(remainingPortions == 0)
        {
            soldOut = true;
        }

        availablePortions = remainingPortions;
    }

    public void disableOffer()
    {
        this.enabled = false;
    }

    public void enableOffer()
    {
        this.enabled = true;
    }

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }

    private void requireNotSoldOut()
    {
        if(soldOut)
        {
            throw new ConflictException("All take away dishes are sold out");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof DishSuggestion)) return false;
        TakeAwayOffer other = (TakeAwayOffer) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
