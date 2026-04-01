package com.brandex.service;

import com.brandex.datastructures.LinkedList;
import com.brandex.datastructures.Stack;
import com.brandex.models.CartItem;
import com.brandex.models.Product;

public class CartService {
    private static CartService instance;
    private final LinkedList<CartItem> cartItems = new LinkedList<>();
    private final Stack<CartCommand> undoStack = new Stack<>();
    private final Stack<CartCommand> redoStack = new Stack<>();

    private CartService() {}

    public static CartService getInstance() {
        if (instance == null) instance = new CartService();
        return instance;
    }

    public void addToCart(Product product, int qty) {
        CartCommand cmd = new AddToCartCommand(cartItems, product, qty);
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear(); // New actions always clear redo history
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            CartCommand cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            CartCommand cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }

    public double getCartTotal() {
        final double[] total = {0.0};
        cartItems.traverse(item -> total[0] += item.getLineTotal());
        return total[0];
    }

    public LinkedList<CartItem> getCartItems() { return cartItems; }
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}