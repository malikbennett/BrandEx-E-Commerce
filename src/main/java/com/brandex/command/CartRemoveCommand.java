package com.brandex.command;

import com.brandex.models.CartItem;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Cart;
import com.brandex.models.Product;
import com.brandex.repository.CartRepository;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;

// The command class for removing an item from the cart.
public class CartRemoveCommand implements Command {
    private final LinkedList<CartItem> cartList;
    private final Cart cart;
    private final CartRepository cartRepo = CartRepository.getInstance();
    private final String productId;
    private final int quantity;
    private int previousQuantity = 0;
    private String cartItemId;
    private boolean wasDeleted = false;

    // Constructor for the CartRemoveCommand class.
    public CartRemoveCommand(LinkedList<CartItem> cart, String productId, int quantity) {
        this.cartList = cart;
        this.cart = CartService.getInstance().getCurrentCart();
        this.productId = productId;
        this.quantity = quantity;
    }

    // Executes the command to remove an item from the cart.
    @Override
    public void execute() {
        CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
        if (item == null)
            throw new IllegalArgumentException("Item not found in cart.");

        Product product = ProductService.getInstance().searchById(this.productId);
        double price = (product != null) ? product.getPrice() : 0.0;

        this.cartItemId = item.getId();
        this.previousQuantity = item.getQuantity();
        int newQuantity = previousQuantity - this.quantity;

        if (newQuantity <= 0) {
            cartRepo.deleteCartItem(this.cartItemId);
            this.cartList.remove(item);
            this.wasDeleted = true;
        } else {
            double newTotalItemPrice = price * newQuantity;
            cartRepo.updateCartItem(this.cartItemId, "quantity", newQuantity);
            cartRepo.updateCartItem(this.cartItemId, "total_price", newTotalItemPrice);
            item.setQuantity(newQuantity);
            item.setTotalPrice(newTotalItemPrice);
            this.wasDeleted = false;
        }
        CartService.getInstance().syncCartTotalWithDatabase();
    }

    // Undoes the command to remove an item from the cart.
    @Override
    public void undo() {
        Product product = ProductService.getInstance().searchById(this.productId);
        double price = (product != null) ? product.getPrice() : 0.0;

        if (wasDeleted) {
            double restoredTotal = price * this.previousQuantity;
            cartRepo.createCartItem(this.cart.getId(), this.productId, this.previousQuantity, restoredTotal);
            CartItem newItem = cartRepo.getCartItemByProduct(this.cart.getId(), this.productId);
            this.cartList.insert(newItem);
        } else {
            CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
            if (item != null) {
                double restoredTotal = price * this.previousQuantity;
                cartRepo.updateCartItem(this.cartItemId, "quantity", this.previousQuantity);
                cartRepo.updateCartItem(this.cartItemId, "total_price", restoredTotal);
                item.setQuantity(this.previousQuantity);
                item.setTotalPrice(restoredTotal);
            }
        }
        CartService.getInstance().syncCartTotalWithDatabase();
    }
}
