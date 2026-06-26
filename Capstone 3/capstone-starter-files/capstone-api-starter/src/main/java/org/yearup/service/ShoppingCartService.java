package org.yearup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yearup.models.CartItem;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.repository.ShoppingCartRepository;

import java.util.List;

@Service
public class ShoppingCartService
{
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductService productService;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, ProductService productService)
    {
        this.shoppingCartRepository = shoppingCartRepository;
        this.productService = productService;
    }

    // Build the response cart: one row per product, with full product details looked up.
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();

        List<CartItem> rows = shoppingCartRepository.findByUserId(userId);
        for (CartItem row : rows)
        {
            Product product = productService.getById(row.getProductId());
            if (product == null) continue; // product was deleted; skip it

            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(row.getQuantity());
            cart.add(item);                // keyed by productId inside the model
        }
        return cart;
    }

    // Add 1, or bump quantity by 1 if the product is already in the cart.
    public ShoppingCart addProduct(int userId, int productId)
    {
        CartItem existing = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (existing == null)
        {
            CartItem row = new CartItem();
            row.setUserId(userId);
            row.setProductId(productId);
            row.setQuantity(1);
            shoppingCartRepository.save(row);
        }
        else
        {
            existing.setQuantity(existing.getQuantity() + 1);
            shoppingCartRepository.save(existing);
        }
        return getByUserId(userId);
    }

    // Set an exact quantity for a product already in the cart.
    public ShoppingCart updateQuantity(int userId, int productId, int quantity)
    {
        CartItem existing = shoppingCartRepository.findByUserIdAndProductId(userId, productId);
        if (existing != null)
        {
            existing.setQuantity(quantity);
            shoppingCartRepository.save(existing);
        }
        return getByUserId(userId);
    }

    // Remove all of this user's cart rows.
    @Transactional   // needed because deleteByUserId runs a delete query
    public ShoppingCart clearCart(int userId)
    {
        shoppingCartRepository.deleteByUserId(userId);
        return getByUserId(userId);
    }
}
