package com.brandex.service;

import com.brandex.models.CartItem;
import com.brandex.models.Product;
import com.brandex.datastructures.LinkedList;

// --- COMMAND FOR ADDING OR UPDATING QUANTITY ---
class AddToCartCommand implements CartCommand {
    private final LinkedList<CartItem> cart;
    private final Product product;
    private final int quantity;
    private int previousQuantity = 0;
    private boolean wasNewItem = false;

    public AddToCartCommand(LinkedList<CartItem> cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    @Override
    public void execute() {
        CartItem existing = find(product.getId());
        if (existing != null) {
            previousQuantity = existing.getQuantity();
            existing.setQuantity(previousQuantity + quantity);
            wasNewItem = false;
        } else {
            cart.insert(new CartItem(product, quantity));
            wasNewItem = true;
        }
    }

    @Override
    public void undo() {
        CartItem item = find(product.getId());
        if (wasNewItem) {
            // Logic to remove item from your specific LinkedList
            // cart.remove(item); 
        } else if (item != null) {
            item.setQuantity(previousQuantity);
        }
    }

    private CartItem find(String id) {
        final CartItem[] found = {null};
        cart.traverse(item -> { if(item.getProduct().getId().equals(id)) found[0] = item; });
        return found[0];
    }
}