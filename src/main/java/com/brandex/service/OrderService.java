package com.brandex.service;

import com.brandex.datastructures.LinkedList;
import com.brandex.models.Order;
import com.brandex.models.User;
import com.brandex.models.enums.OrderStatus;
import com.brandex.repository.OrderRepository;
import com.brandex.repository.UserRepository;
import com.brandex.utilities.EmailSender;

import java.util.List;

/**
 * OrderService manages all order operations for the admin panel.
 *
 * FIFO Queue: Uses the custom LinkedList as a queue — pending orders are inserted
 * at the tail and dequeued from the head (FIFO). This gives O(1) enqueue/dequeue
 * since the LinkedList maintains both head and tail pointers.
 */
public class OrderService {

    private static OrderService instance;

    private final OrderRepository orderRepo = OrderRepository.getInstance();
    private final UserRepository userRepo = UserRepository.getInstance();

    // FIFO queue: pending orders only — head = oldest (next to process)
    private final LinkedList<Order> pendingQueue = new LinkedList<>((a, b) -> a.getId().compareTo(b.getId()));

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    /** Load all PENDING orders from DB into the FIFO queue (oldest first). */
    public void loadPendingQueue() {
        pendingQueue.clear();
        List<Order> pending = orderRepo.getOrdersByStatus(OrderStatus.PENDING);
        for (Order o : pending) {
            pendingQueue.insert(o); // insert to tail — FIFO enqueue
        }
    }

    /** Peek at the head of the queue without removing it. */
    public Order peekNext() {
        if (pendingQueue.getHead() == null) return null;
        return pendingQueue.getHead().getData();
    }

    /**
     * Dequeue the next pending order (FIFO). Advances status to PROCESSING
     * in the database and removes it from the in-memory queue.
     */
    public Order dequeue() {
        if (pendingQueue.getHead() == null) return null;
        Order order = pendingQueue.getHead().getData();
        pendingQueue.remove(order); // removes first occurrence (head)
        // Advance status to PROCESSING
        orderRepo.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);
        order.setStatus(OrderStatus.PROCESSING);
        return order;
    }

    /** Fetch all orders for the full admin table view. */
    public List<Order> getAllOrders() {
        return orderRepo.getAllOrders();
    }

    /** Update an order's status in DB. */
    public void updateStatus(Order order, OrderStatus newStatus) {
        orderRepo.updateOrderStatus(order.getId(), newStatus);
        order.setStatus(newStatus);
    }

    /**
     * Mark order as shipped, persist to DB, and send a shipping notification
     * email to the customer asynchronously.
     */
    public void markAsShipped(Order order) {
        updateStatus(order, OrderStatus.SHIPPED);

        // Async email — runs in background thread so UI is never blocked
        Thread emailThread = new Thread(() -> {
            try {
                User customer = userRepo.findById(order.getUserId());
                if (customer == null || customer.getEmail() == null) {
                    System.err.println("Order email skipped: customer not found for userId=" + order.getUserId());
                    return;
                }
                String subject = "Your BrandEx Order #" + order.getOrderNumber() + " Has Shipped!";
                String body = buildShippingEmail(customer, order);
                EmailSender.send(customer.getEmail(), subject, body);
                System.out.println("Shipping email sent to " + customer.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send shipping email: " + e.getMessage());
                e.printStackTrace();
            }
        }, "email-thread-" + order.getOrderNumber());
        emailThread.setDaemon(true);
        emailThread.start();
    }

    public int getPendingQueueSize() {
        final int[] count = {0};
        pendingQueue.traverse(o -> count[0]++);
        return count[0];
    }

    public boolean isPendingQueueEmpty() {
        return pendingQueue.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildShippingEmail(User customer, Order order) {
        return String.format("""
                Hello %s,

                Great news! Your BrandEx order has been shipped and is on its way.

                ─────────────────────────────────────
                ORDER DETAILS
                ─────────────────────────────────────
                Order Number  :  %s
                Order Total   :  $%.2f
                Payment       :  %s
                Ship-To       :  %s

                Status        :  SHIPPED

                ─────────────────────────────────────
                ESTIMATED DELIVERY
                ─────────────────────────────────────
                Your order is expected to arrive within 3–5 business days.
                You will receive another update once it has been delivered.

                Thank you for shopping with BrandEx!

                Regards,
                The BrandEx Fulfillment Team
                """,
                customer.getFirstName(),
                order.getOrderNumber(),
                order.getTotal(),
                order.getPaymentMethod().toString(),
                order.getShippingAddress()
        );
    }
}
