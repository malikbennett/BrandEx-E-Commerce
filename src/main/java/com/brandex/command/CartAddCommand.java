package com.brandex.command;

import com.brandex.models.CartItem;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Cart;
import com.brandex.repository.CartRepository;
import com.brandex.service.CartService;

public class CartAddCommand implements Command {
    private final LinkedList<CartItem> cartList;
    private final Cart cart;
    private final CartRepository cartRepo = CartRepository.getInstance();
    private final String productId;
    private final int quantity;
    private int previousQuantity = 0;
    private boolean isNewItem = false;

    public CartAddCommand(LinkedList<CartItem> cart, String productId, int quantity) {
        this.cartList = cart;
        this.productId = productId;
        this.quantity = quantity;
        this.cart = CartService.getInstance().getCurrentCart();
    }

    @Override
    public void execute() {
        // find the item that was added
        CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
        // if it doesnt exist then create it
        if (item == null) {
            cartRepo.createCartItem(this.cart.getId(), this.productId, this.quantity);
            item = cartRepo.getCartItem("cart_id", this.cart.getId());
            this.cartList.insert(item);
            isNewItem = true;
        } else { // if the item exist then update the quantity
            previousQuantity = item.getQuantity();
            cartRepo.updateCartItem(item.getId(), "quantity", String.valueOf(this.quantity + previousQuantity));
            item.setQuantity(this.quantity + previousQuantity);
            isNewItem = false;
        }
    }

    @Override
    public void undo() {
        // find the item that was added
        CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
        if (item != null) {
            // if it was a new item then remove it
            if (isNewItem) {
                this.cartList.remove(item);
                cartRepo.deleteCartItem(item.getId());
            } else { // if it was an existing item then update the quantity
                cartRepo.updateCartItem(item.getId(), "quantity", String.valueOf(this.previousQuantity));
                item.setQuantity(this.previousQuantity);
            }
        }
    }
}
