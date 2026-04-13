package com.brandex.service;

import com.brandex.command.CartAddCommand;
import com.brandex.command.CartRemoveCommand;
import com.brandex.command.Command;
import com.brandex.datastructures.LinkedList;
import com.brandex.datastructures.Stack;
import com.brandex.models.Cart;
import com.brandex.models.CartItem;
import com.brandex.models.Order;
import com.brandex.models.OrderItem;
import com.brandex.models.User;
import com.brandex.models.enums.OrderStatus;
import com.brandex.models.enums.PaymentMethod;
import com.brandex.repository.CartRepository;
import com.brandex.repository.OrderRepository;
import com.brandex.utilities.EmailSender;
import java.util.Random;

// The service class for managing the user's cart.
public class CartService {
    private static CartService instance;
    private Cart currentCart;
    private final LinkedList<CartItem> cartList = new LinkedList<>(
            (a, b) -> a.getProductId().compareTo(b.getProductId()));
    private final CartRepository cartRepo = CartRepository.getInstance();
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    private boolean loaded = false;

    // Returns the instance of the CartService
    public static CartService getInstance() {
        if (instance == null)
            instance = new CartService();
        return instance;
    }

    // Load the users cart from database
    public void loadCart() {
        this.currentCart = cartRepo.getCart("user_id", AuthService.getInstance().getCurrentUser().getId());
        if (this.currentCart == null)
            throw new IllegalArgumentException("Cart not found for user.");
        this.cartRepo.listCartItems(this.currentCart.getId()).traverse(cartItem -> {
            this.cartList.insert(cartItem);
        });
        this.loaded = true;
    }

    // Returns true if the cart is loaded
    public boolean isLoaded() {
        return this.loaded;
    }

    // Returns the current cart
    public Cart getCurrentCart() {
        return this.currentCart;
    }

    // Returns the list of cart items
    public LinkedList<CartItem> getCartItems() {
        if (this.cartList.isEmpty()) {
        }
        return this.cartList;
    }

    // Create a cart for user on load
    public void createCart() {
        Cart cart = new Cart();
        cart.setUserId(AuthService.getInstance().getCurrentUser().getId());
        this.cartRepo.createCart(cart);
    }

    // Clears the cart
    public void clearCart() {
        this.currentCart = null;
        this.cartList.clear();
        this.undoStack.clear();
        this.redoStack.clear();
        this.loaded = false;
    }

    // Add item to cart
    public void addItem(String productId, int qty) {
        if (!this.isLoaded())
            loadCart();
        System.out.println("Adding item to cart: " + productId + " " + qty);
        Command cmd = new CartAddCommand(this.cartList, productId, qty);
        cmd.execute();
        this.undoStack.push(cmd);
        this.redoStack.clear();
    }

    // Remove item from cart
    public void removeItem(String productId, int qty) {
        if (!this.isLoaded())
            loadCart();
        System.out.println("Removing item from cart: " + productId + " " + qty);
        Command cmd = new CartRemoveCommand(this.cartList, productId, qty);
        cmd.execute();
        this.undoStack.push(cmd);
        this.redoStack.clear();
    }

    // Checks out the cart
    public void checkout(String shippingAddress, PaymentMethod paymentMethod) {
        System.out.println("Checking out cart");

        if (this.cartList.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(AuthService.getInstance().getCurrentUser().getId());
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setTotal(getCartTotal());

        OrderRepository.getInstance().createOrder(order);

        this.cartList.traverse(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            OrderRepository.getInstance().createOrderItem(orderItem);
        });

        OrderService.getInstance().enqueue(order);

        if (this.currentCart != null) {
            this.cartRepo.clearCartItems(this.currentCart.getId());
            this.cartRepo.updateCartTotalPrice(this.currentCart.getId(), 0.0);
        }

        // Send confirmation email
        User customer = AuthService.getInstance().getCurrentUser();
        EmailSender.sendAsync(
                customer.getEmail(),
                "Order Confirmed - #" + order.getOrderNumber(),
                OrderService.getInstance().buildConfirmationEmail(customer, order),
                () -> System.out.println("Confirmation email sent for " + order.getOrderNumber()),
                error -> System.err.println("Confirmation email PERMANENT failure: " + error));

        clearCart();
    }

    // Generates an order number
    private String generateOrderNumber() {
        Random rnd = new Random();
        int number = 10000000 + rnd.nextInt(90000000);
        return "ORD-" + number;
    }

    // Undoes the last cart action
    public void undo() {
        if (!undoStack.isEmpty()) {
            System.out.println("Undo cart action");
            Command cmd = undoStack.pop();
            cmd.undo();
            this.redoStack.push(cmd);
        }
    }

    // Redoes the last cart action
    public void redo() {
        if (!redoStack.isEmpty()) {
            System.out.println("Redo cart action");
            Command cmd = this.redoStack.pop();
            cmd.execute();
            this.undoStack.push(cmd);
        }
    }

    // Returns the total price of the cart
    public double getCartTotal() {
        final double[] total = { 0.0 };
        this.cartList.traverse(item -> total[0] += item.getTotalPrice());
        return total[0];
    }

    // Syncs the cart total with the database
    public void syncCartTotalWithDatabase() {
        if (this.currentCart != null) {
            this.cartRepo.updateCartTotalPrice(this.currentCart.getId(), getCartTotal());
        }
    }

}
