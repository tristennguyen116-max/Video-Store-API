package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.Order;
import org.yearup.models.User;
import org.yearup.service.OrderService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController
{
    private final OrderService orderService;
    private final UserService userService;

    public OrdersController(OrderService orderService, UserService userService)
    {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Order> checkout(Principal principal)
    {
        User user = userService.getByUserName(principal.getName());
        Order order = orderService.checkout(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);   // 201
    }
}