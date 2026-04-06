package app.persistence.entities;

import app.exceptions.ConflictException;
import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "take_away_offer")
public class TakeAwayOffer implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "soldout")
    private boolean soldOut;

    @Column(name = "offered_portions", nullable = false)
    private int offeredPortions;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "available_portions_left")
    private int availablePortions ;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    Dish dish;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    User createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TakeAwayOffer(int offeredPortions, User createdBy, Dish dish)
    {
        ValidationUtil.validatePositive(offeredPortions, "Offered portions");
        ValidationUtil.validateNotNull(createdBy, "User");
        ValidationUtil.validateNotNull(dish, "Dish");

        this.enabled = true;
        this.soldOut = false;
        this.offeredPortions = offeredPortions;
        this.availablePortions = offeredPortions;
        this.createdBy = createdBy;
        this.dish = dish;
    }

    public void sellPortions(int quantity)
    {
        ValidationUtil.validatePositive(quantity, "Quantity");
        requireEnabled();
        requireNotSoldOut();

        int remainingPortions = availablePortions - quantity;

        if (remainingPortions < 0)
        {
            throw new ConflictException("Not enough portions left for your order");
        }

        availablePortions = remainingPortions;

        if (remainingPortions == 0)
        {
            soldOut = true;
            enabled = false;
        }
    }

    public void disableOffer()
    {
        this.enabled = false;
    }

    public void enableOffer()
    {
        if (soldOut)
        {
            throw new ConflictException("Cannot enable a sold out offer");
        }
        this.enabled = true;
    }

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDate.now();
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

    private void requireEnabled()
    {
        if (!enabled)
        {
            throw new ConflictException("Take away offer is not enabled");
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TakeAwayOffer)) return false;
        TakeAwayOffer other = (TakeAwayOffer) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
