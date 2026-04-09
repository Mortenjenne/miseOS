package app.mappers;

import app.dtos.takeaway.TakeAwayOfferReferenceDTO;
import app.dtos.takeaway.TakeAwayOrderDTO;
import app.dtos.takeaway.TakeAwayOrderLineDTO;
import app.persistence.entities.TakeAwayOrder;
import app.persistence.entities.TakeAwayOrderLine;

import java.util.List;

public class TakeAwayOrderMapper
{
    private TakeAwayOrderMapper (){}

    public static TakeAwayOrderDTO toDTO(TakeAwayOrder order)
    {
        List<TakeAwayOrderLineDTO> lines = order.getOrderLines()
            .stream()
            .map(TakeAwayOrderMapper::toLineDTO)
            .toList();

        return new TakeAwayOrderDTO(
            order.getId(),
            order.getTotalOrderLines(),
            order.getTotalItems(),
            UserMapper.toReferenceDTO(order.getCustomer()),
            lines,
            order.getTotalPrice(),
            order.getOrderStatus(),
            order.getOrderedAt(),
            order.getCreatedAt()
        );
    }

    private static TakeAwayOrderLineDTO toLineDTO(TakeAwayOrderLine line)
    {
        TakeAwayOfferReferenceDTO offerReferenceDTO = TakeAwayOfferMapper.toReferenceDTO(line.getTakeAwayOffer());

        return new TakeAwayOrderLineDTO(
            line.getId(),
            offerReferenceDTO,
            line.getQuantity(),
            line.getPriceAtPurchase()
        );
    }
}
