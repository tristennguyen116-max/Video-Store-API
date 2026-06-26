package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import org.yearup.service.ShoppingCartService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")   // every action here requires a logged-in user (else 401)
public class ShoppingCartController
{
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;

    public ShoppingCartController(ShoppingCartService shoppingCartService, UserService userService)
    {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
    }

    // turn the logged-in username (from the token) into the database userId
    private int getUserId(Principal principal)
    {
        User user = userService.getByUserName(principal.getName());
        return user.getId();
    }

    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        return shoppingCartService.getByUserId(getUserId(principal));
    }

    @PostMapping("products/{productId}")
    public ResponseEntity<ShoppingCart> addProduct(@PathVariable int productId, Principal principal)
    {
        ShoppingCart cart = shoppingCartService.addProduct(getUserId(principal), productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);   // 201
    }

    @PutMapping("products/{productId}")
    public ShoppingCart updateProduct(@PathVariable int productId,
                                      @RequestBody ShoppingCartItem item,
                                      Principal principal)
    {
        // only the quantity field of the body matters
        return shoppingCartService.updateQuantity(getUserId(principal), productId, item.getQuantity());
    }

    @DeleteMapping
    public ShoppingCart clearCart(Principal principal)
    {
        return shoppingCartService.clearCart(getUserId(principal));    // returns empty cart, 200
    }
}
