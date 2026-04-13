package com.brandex.service;

import com.brandex.datastructures.Queue;
import com.brandex.models.Order;
import com.brandex.models.User;
import com.brandex.models.enums.OrderStatus;
import com.brandex.repository.OrderRepository;
import com.brandex.utilities.EmailSender;

import java.util.function.Consumer;

import com.brandex.datastructures.LinkedList;

// The service class for managing orders.
public class OrderService {
    private static OrderService instance;
    private final LinkedList<Order> orderList = new LinkedList<>((a, b) -> 0);
    private final OrderRepository orderRepo = OrderRepository.getInstance();
    private final UserService userService = UserService.getInstance();
    private final Queue<Order> pendingQueue = new Queue<>();
    private boolean loaded = false;

    // Returns the instance of the OrderService
    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    // Load all orders from database
    public void loadOrders() {
        this.orderRepo.getAllOrders().traverse(order -> this.orderList.insert(order));
        this.loaded = true;
    }

    // Returns true if the orders are loaded
    public boolean isLoaded() {
        return this.loaded;
    }

    // Loads the pending queue
    public void loadPendingQueue() {
        this.pendingQueue.clear();
        LinkedList<Order> pending = this.orderRepo.getOrdersByStatus(OrderStatus.PENDING);
        pending.traverse(order -> this.pendingQueue.enqueue(order));
    }

    // Iterates over all orders
    public void forEachOrder(Consumer<Order> action) {
        this.orderList.traverse(action);
    }

    // Enqueues an order
    public void enqueue(Order order) {
        this.pendingQueue.enqueue(order);
        this.orderList.insert(order);
    }

    // Peeks at the next order in the queue
    public Order peekNext() {
        return this.pendingQueue.peek();
    }

    // Dequeues the next order from the queue
    public Order dequeue() {
        if (this.pendingQueue.isEmpty())
            return null;
        Order order = this.pendingQueue.dequeue();
        this.orderRepo.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);
        order.setStatus(OrderStatus.PROCESSING);
        return order;
    }

    // Returns the list of orders
    public LinkedList<Order> getOrderList() {
        return this.orderList;
    }

    // Returns the list of orders for a specific user
    public LinkedList<Order> getOrdersByUserId(String userId) {
        LinkedList<Order> userOrders = new LinkedList<>((a, b) -> 0);
        this.orderList.traverse(order -> {
            if (order.getUserId().equals(userId)) {
                userOrders.insert(order);
            }
        });
        return userOrders;
    }

    // Reloads the orders
    public void reloadOrders() {
        this.orderList.clear();
        this.loadOrders();
        this.loadPendingQueue();
    }

    // Updates the status of an order
    public void updateStatus(Order order, OrderStatus newStatus) {
        updateStatus(order, newStatus, null, null);
    }

    // Updates the status of an order
    public void updateStatus(Order order, OrderStatus newStatus, Runnable onSuccess, Consumer<String> onFailure) {
        this.orderRepo.updateOrderStatus(order.getId(), newStatus);
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            User customer = this.userService.searchById(order.getUserId());
            if (customer != null && customer.getEmail() != null) {
                EmailSender.sendAsync(
                        customer.getEmail(),
                        "Your BrandEx Order Has Been Delivered!",
                        buildDeliveryEmail(customer, order),
                        () -> {
                            System.out.println("Delivery email sent: " + order.getOrderNumber());
                            if (onSuccess != null)
                                onSuccess.run();
                        },
                        error -> {
                            System.err.println("Delivery email failure: " + error);
                            if (onFailure != null)
                                onFailure.accept(error);
                        });
            } else if (onSuccess != null) {
                onSuccess.run(); // No email to send, but status update succeeded
            }
        } else if (onSuccess != null) {
            onSuccess.run();
        }
    }

    // Marks an order as shipped
    public void markAsShipped(Order order) {
        markAsShipped(order, null, null);
    }

    // Marks an order as shipped
    public void markAsShipped(Order order, Runnable onSuccess, Consumer<String> onFailure) {
        this.updateStatus(order, OrderStatus.SHIPPED);

        User customer = this.userService.searchById(order.getUserId());
        if (customer == null || customer.getEmail() == null) {
            System.err.println("Order email skipped: customer not found for userId=" + order.getUserId());
            if (onSuccess != null)
                onSuccess.run();
            return;
        }

        EmailSender.sendAsync(
                customer.getEmail(),
                "Your BrandEx Order #" + order.getOrderNumber() + " Has Shipped!",
                buildShippingEmail(customer, order),
                () -> {
                    System.out.println("Shipping email sent: " + order.getOrderNumber());
                    if (onSuccess != null)
                        onSuccess.run();
                },
                error -> {
                    System.err.println("Shipping email failure: " + error);
                    if (onFailure != null)
                        onFailure.accept(error);
                });
    }

    // Returns the size of the pending queue
    public int getPendingQueueSize() {
        return this.pendingQueue.size();
    }

    // Returns true if the pending queue is empty
    public boolean isPendingQueueEmpty() {
        return this.pendingQueue.isEmpty();
    }

    // Builds the confirmation email
    public String buildConfirmationEmail(User customer, Order order) {
        return String.format(
                """
                        Hello %s,

                        Thank you for your order! We've successfully received your purchase and it's now being prepared for fulfillment.

                        ─────────────────────────────────────
                        ORDER CONFIRMATION
                        ─────────────────────────────────────
                        Order Number  :  %s
                        Order Total   :  $%.2f
                        Payment       :  %s
                        Address       :  %s

                        Status        :  PENDING / CONFIRMED

                        We will notify you as soon as your items have been shipped.

                        Regards,
                        The BrandEx Team
                        """,
                customer.getFirstName(),
                order.getOrderNumber(),
                order.getTotal(),
                order.getPaymentMethod().toString(),
                order.getShippingAddress());
    }

    // Builds the shipping email
    private String buildShippingEmail(User customer, Order order) {
        return String.format("""
                Hello %s,

                Great news! Your BrandEx order has been shipped and is on its way.

                ─────────────────────────────────────
                SHIPPING UPDATE
                ─────────────────────────────────────
                Order Number  :  %s
                Order Total   :  $%.2f
                Payment       :  %s
                Address       :  %s

                Status        :  SHIPPED

                ─────────────────────────────────────
                ESTIMATED DELIVERY
                ─────────────────────────────────────
                Your order is expected to arrive within 3–5 business days.
                You will receive another update once it has been delivered.

                Thank you for shopping with BrandEx!

                Regards,
                The BrandEx Team
                """,
                customer.getFirstName(),
                order.getOrderNumber(),
                order.getTotal(),
                order.getPaymentMethod().toString(),
                order.getShippingAddress());
    }

    // Builds the delivery email
    public String buildDeliveryEmail(User customer, Order order) {
        return String.format("""
                Hello %s,

                Your order from BrandEx has been officially delivered. Enjoy your purchase!

                ─────────────────────────────────────
                DELIVERY DETAILS
                ─────────────────────────────────────
                Order Number  :  %s
                Order Total   :  $%.2f
                Final Status  :  DELIVERED

                If you have any issues with your delivery or the products received, please contact our support team.

                Thank you for choosing BrandEx!

                Regards,
                The BrandEx Team
                """,
                customer.getFirstName(),
                order.getOrderNumber(),
                order.getTotal());
    }

    // Clears the orders
    public void clearOrders() {
        this.orderList.clear();
        this.pendingQueue.clear();
        this.loaded = false;
    }
}
