package app.persistence.entities;

import app.utils.ValidationUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "take_away_order_line")
public class TakeAwayOrderLine implements IEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private TakeAwayOrder order;

    @ManyToOne
    @JoinColumn(name = "take_away_offer_id", nullable = false)
    private TakeAwayOffer offer;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_at_purchase", nullable = false)
    private double priceAtPurchase;

    public TakeAwayOrderLine(TakeAwayOrder order, TakeAwayOffer offer, int quantity)
    {
        ValidationUtil.validateNotNull(order, "Order");
        ValidationUtil.validateNotNull(offer, "Offer");
        ValidationUtil.validatePositive(quantity, "Quantity");

        this.order = order;
        this.offer = offer;
        this.quantity = quantity;
        this.priceAtPurchase = offer.getPrice() * quantity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TakeAwayOrderLine)) return false;
        TakeAwayOrderLine other = (TakeAwayOrderLine) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
