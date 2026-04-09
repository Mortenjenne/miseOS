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
import java.util.HashSet;
import java.util.Set;

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
    private User customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TakeAwayOrderLine> orderLines = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public TakeAwayOrder(User customer)
    {
        ValidationUtil.validateNotNull(customer, "Customer");

        this.customer = customer;
        this.orderStatus = OrderStatus.RESERVED;
        this.orderedAt = LocalDateTime.now();
    }

    public void setOrderPaid(User user)
    {
        ValidationUtil.validateNotNull(user, "User");

        if (this.orderStatus == OrderStatus.CANCELLED)
        {
            throw new ConflictException("Cannot pay for a cancelled order");
        }

        if (!isHeadChefOrSousChef(user))
        {
            throw new UnauthorizedActionException("Only head or sous chef can manage payments");
        }

        this.orderStatus = OrderStatus.PAID;
    }

    public void cancelOrder(User user)
    {
        if (this.orderStatus == OrderStatus.PAID)
        {
            throw new ConflictException("Cannot cancel an order that is already payed");
        }

        if (!isHeadChefOrSousChef(user) && !isOwner(user))
        {
            throw new UnauthorizedActionException("Only owners, head chefs and sous chefs can cancel order");
        }

        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void addOrderLine(TakeAwayOrderLine line)
    {
        ValidationUtil.validateNotNull(line, "Order line");
        orderLines.add(line);
    }

    public double getTotalPrice()
    {
        return orderLines.stream()
            .mapToDouble(TakeAwayOrderLine::getPriceAtPurchase)
            .sum();
    }

    public int getTotalItems()
    {
        return orderLines.stream()
            .mapToInt(TakeAwayOrderLine::getQuantity)
            .sum();
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
        return user.isHeadChef() || user.isSousChef();
    }

    private boolean isOwner(User user)
    {
        return user.getId().equals(customer.getId());
    }
}
