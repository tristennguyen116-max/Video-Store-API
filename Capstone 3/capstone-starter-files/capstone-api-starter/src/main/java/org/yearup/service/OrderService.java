package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.repository.OrderLineItemRepository;
import org.yearup.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderService
{
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final ProfileService profileService;

    public OrderService(OrderRepository orderRepository,
                        OrderLineItemRepository orderLineItemRepository,
                        ShoppingCartService shoppingCartService,
                        ProfileService profileService)
    {
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.shoppingCartService = shoppingCartService;
        this.profileService = profileService;
    }

    @Transactional   // all-or-nothing: if anything fails, nothing is half-saved
    public Order checkout(int userId)
    {
        ShoppingCart cart = shoppingCartService.getByUserId(userId);

        // can't check out an empty cart
        if (cart.getItems().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty.");

        // ship to the address on the user's profile
        Profile profile = profileService.getByUserId(userId);

        Order order = new Order();
        order.setUserId(userId);
        order.setDate(LocalDateTime.now());
        order.setAddress(profile != null ? profile.getAddress() : "");
        order.setCity(profile != null ? profile.getCity() : "");
        order.setState(profile != null ? profile.getState() : "");
        order.setZip(profile != null ? profile.getZip() : "");
        order.setShippingAmount(0);

        Order savedOrder = orderRepository.save(order);   // now savedOrder has its new id

        // one line item per product in the cart
        for (Map.Entry<Integer, ShoppingCartItem> entry : cart.getItems().entrySet())
        {
            ShoppingCartItem cartItem = entry.getValue();

            OrderLineItem line = new OrderLineItem();
            line.setOrderId(savedOrder.getOrderId());
            line.setProductId(cartItem.getProduct().getProductId());
            line.setSalesPrice(cartItem.getProduct().getPrice());
            line.setQuantity(cartItem.getQuantity());
            line.setDiscount(0);
            orderLineItemRepository.save(line);
        }

        // the cart is now an order, so empty it
        shoppingCartService.clearCart(userId);

        return savedOrder;
    }
}