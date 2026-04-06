package app.persistence.entities;

import app.enums.OrderStatus;
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
@Table(name = "take_away_order")
public class TakeAwayOrder implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    User customer;

    @ManyToOne
    @JoinColumn(name = "take_away_offer_id", nullable = false)
    TakeAwayOffer takeAwayOffer;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_at_purchase", nullable = false)
    private double priceAtPurchase;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public TakeAwayOrder(User customer, TakeAwayOffer takeAwayOffer, int quantity)
    {
        ValidationUtil.validateNotNull(customer, "Customer");
        ValidationUtil.validateNotNull(takeAwayOffer, "Take away offer");
        ValidationUtil.validatePositive(quantity, "Quantity");

        takeAwayOffer.sellPortions(quantity);

        this.customer = customer;
        this.takeAwayOffer = takeAwayOffer;
        this.orderStatus = OrderStatus.RESERVED;
        this.quantity = quantity;
        this.priceAtPurchase = takeAwayOffer.getPrice() * quantity;
        this.orderedAt = LocalDateTime.now();
    }

    public void setOrderPayed(User user)
    {
        ValidationUtil.validateNotNull(user, "User");

        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new ConflictException("Cannot pay for a cancelled order");
        }

        if (!isHeadChefOrSousChef(user))
        {
            throw new UnauthorizedActionException("Only head or sous chef can manage payments");
        }

        this.orderStatus = OrderStatus.PAYED;
    }

    public void cancelOrder(User user)
    {
        if (this.orderStatus == OrderStatus.PAYED) {
            throw new ConflictException("Cannot cancel an order that is already payed");
        }

        if (!isHeadChefOrSousChef(user) && !isOwner(user))
        {
            throw new UnauthorizedActionException("Only owners, head chefs and sous chefs can cancel order");
        }
        this.takeAwayOffer.addPortionsBack(this.quantity);
        this.orderStatus = OrderStatus.CANCELLED;
    }

    @PrePersist
    private void onCreate()
    {
        this.createdAt = LocalDate.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TakeAwayOrder)) return false;
        TakeAwayOrder other = (TakeAwayOrder) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

    private boolean isHeadChefOrSousChef(User user)
    {
        return user.isHeadChef() || !user.isSousChef();
    }

    private boolean isOwner(User user)
    {
        return user.getId().equals(customer.getId());
    }
}
