package com.brandex.service;

import com.brandex.command.CartAddCommand;
import com.brandex.command.CartRemoveCommand;
import com.brandex.command.Command;
import com.brandex.datastructures.LinkedList;
import com.brandex.datastructures.Stack;
import com.brandex.models.Cart;
import com.brandex.models.CartItem;
import com.brandex.repository.CartRepository;

public class CartService {
    private static CartService instance;
    private Cart currentCart;
    private final CartRepository cartRepo = CartRepository.getInstance();
    private LinkedList<CartItem> cart = new LinkedList<>((a, b) -> a.getProductId().compareTo(b.getProductId()));
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    public static CartService getInstance() {
        if (instance == null)
            instance = new CartService();
        return instance;
    }

    public Cart getCurrentCart() {
        return this.currentCart;
    }

    public LinkedList<CartItem> getCartItems() {
        return this.cart;
    }

    public void createCart() {
        Cart cart = new Cart();
        cart.setUserId(AuthService.getInstance().getCurrentUser().getId());
        cartRepo.createCart(cart);
    }

    public void loadCart() {
        this.currentCart = cartRepo.getCart("user_id", AuthService.getInstance().getCurrentUser().getId());
        if (this.currentCart == null)
            throw new IllegalArgumentException("Cart not found for user.");
        this.cartRepo.listCartItems(this.currentCart.getId()).traverse(cartItem -> {
            this.cart.insert(cartItem);
        });
    }

    public void addItem(String productId, int qty) {
        Command cmd = new CartAddCommand(this.cart, productId, qty);
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void removeItem(String productId, int qty) {
        Command cmd = new CartRemoveCommand(this.cart, productId, qty);
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
    }

    // public double getCartTotal() {
    // final double[] total = { 0.0 };
    // cart.traverse(item -> total[0] += item.getLineTotal());
    // return total[0];
    // }

}
