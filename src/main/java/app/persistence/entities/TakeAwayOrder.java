package app.persistence.entities;

import app.enums.OrderStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public TakeAwayOrder(User customer, TakeAwayOffer takeAwayOffer, OrderStatus orderStatus, int quantity)
    {
        ValidationUtil.validateNotNull(customer, "Customer");
        ValidationUtil.validateNotNull(takeAwayOffer, "Take away offer");
        ValidationUtil.validateNotNull(orderStatus, "Order status");
        ValidationUtil.validatePositive(quantity, "Quantity");

        this.customer = customer;
        this.takeAwayOffer = takeAwayOffer;
        this.orderStatus = orderStatus;
        this.quantity = quantity;
        this.orderedAt = LocalDateTime.now();
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
}
