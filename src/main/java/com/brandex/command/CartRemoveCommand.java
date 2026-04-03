package com.brandex.command;

import com.brandex.models.CartItem;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Cart;
import com.brandex.repository.CartRepository;
import com.brandex.service.CartService;

public class CartRemoveCommand implements Command {
    private final LinkedList<CartItem> cartList;
    private final Cart cart;
    private final CartRepository cartRepo = CartRepository.getInstance();
    private final String productId;
    private final int quantity;
    private int previousQuantity = 0;
    private boolean isNewItem = false;

    public CartRemoveCommand(LinkedList<CartItem> cart, String productId, int quantity) {
        this.cartList = cart;
        this.cart = CartService.getInstance().getCurrentCart();
        this.productId = productId;
        this.quantity = quantity;
    }

    @Override
    public void execute() {
        // find the item
        CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
        // if the item exist then update the quantity
        if (item == null)
            throw new IllegalArgumentException("Item not found in cart.");
        previousQuantity = item.getQuantity();
        cartRepo.updateCartItem(item.getId(), "quantity", String.valueOf(previousQuantity - this.quantity));
        isNewItem = false;
        item.setQuantity(previousQuantity - this.quantity);
    }

    @Override
    public void undo() {
        if (isNewItem) {
            cartRepo.deleteCartItem(this.cart.getId());
        } else {
            cartRepo.updateCartItem(this.cart.getId(), this.productId, String.valueOf(this.previousQuantity));
        }
    }
}
