package app.routes.resources;

import app.controllers.ITakeAwayOfferController;
import app.controllers.ITakeAwayOrderController;
import app.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TakeAwayRoute
{
    private final ITakeAwayOfferController offerController;
    private final ITakeAwayOrderController orderController;

    public TakeAwayRoute(ITakeAwayOfferController offerController, ITakeAwayOrderController orderController)
    {
        this.offerController = offerController;
        this.orderController = orderController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("takeaway", () ->
            {
                path("offers", () ->
                {
                    get("", offerController::getAll, Role.ANYONE);
                    get("{id}", offerController::getById, Role.ANYONE);
                    post("", offerController::create, Role.HEAD_CHEF, Role.SOUS_CHEF);
                    put("{id}", offerController::update, Role.HEAD_CHEF, Role.SOUS_CHEF);
                    patch("{id}/enable", offerController::enableOffer, Role.HEAD_CHEF, Role.SOUS_CHEF);
                    patch("{id}/disable",offerController::disableOffer, Role.HEAD_CHEF, Role.SOUS_CHEF);
                    delete("{id}", offerController::delete, Role.HEAD_CHEF, Role.SOUS_CHEF);
                });

                path("orders", () ->
                {
                    get("summary", orderController::getSummary, Role.HEAD_CHEF, Role.SOUS_CHEF);
                    get("", orderController::getOrders, Role.CUSTOMER, Role.KITCHEN_STAFF);
                    get("{id}", orderController::getById, Role.CUSTOMER, Role.KITCHEN_STAFF);
                    post("", orderController::placeOrder, Role.CUSTOMER);
                    patch("{id}/pay", orderController::markAsPaid, Role.HEAD_CHEF, Role.SOUS_CHEF);
                    patch("{id}/cancel", orderController::cancelOrder, Role.CUSTOMER, Role.HEAD_CHEF, Role.HEAD_CHEF);
                });
            });
        };
    }
}
