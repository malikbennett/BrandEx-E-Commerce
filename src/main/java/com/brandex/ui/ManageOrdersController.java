package com.brandex.ui;

import com.brandex.models.Order;
import com.brandex.models.User;
import com.brandex.models.enums.OrderStatus;
import com.brandex.repository.UserRepository;
import com.brandex.service.AuthService;
import com.brandex.service.OrderService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ManageOrdersController — Admin Order Processing Panel.
 *
 * Layout: split view with a full orders table on top and a detail / action
 * panel below. The FIFO queue (PENDING orders) is surfaced through the
 * "Dequeue Next" button which pulls the oldest pending order, shifts it to
 * PROCESSING, and shows its details so the admin can act on it.
 */
public class ManageOrdersController {

    // ── Table ────────────────────────────────────────────────────────────────
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> colOrderNum;
    @FXML
    private TableColumn<Order, String> colCustomer;
    @FXML
    private TableColumn<Order, String> colStatus;
    @FXML
    private TableColumn<Order, String> colTotal;
    @FXML
    private TableColumn<Order, String> colDate;
    @FXML
    private TableColumn<Order, Void> colActions;

    // ── Queue badge ───────────────────────────────────────────────────────────
    @FXML
    private Label queueSizeLabel;

    // ── Detail panel ─────────────────────────────────────────────────────────
    @FXML
    private Label detailOrderNum;
    @FXML
    private Label detailCustomer;
    @FXML
    private Label detailEmail;
    @FXML
    private Label detailAddress;
    @FXML
    private Label detailPayment;
    @FXML
    private Label detailTotal;
    @FXML
    private Label detailDate;
    @FXML
    private Label detailStatus;
    @FXML
    private Label statusMessage;

    // ── Action buttons ────────────────────────────────────────────────────────
    @FXML
    private Button dequeueBtn;
    @FXML
    private Button markProcessingBtn;
    @FXML
    private Button markShippedBtn;
    @FXML
    private Button markDeliveredBtn;

    // ── Search ───────────────────────────────────────────────────────────────
    @FXML
    private TextField searchField;

    // ─────────────────────────────────────────────────────────────────────────

    private final OrderService orderService = OrderService.getInstance();
    private final UserRepository userRepo = UserRepository.getInstance();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /** The order currently loaded in the detail panel. */
    private Order selectedOrder = null;

    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();

    // =========================================================================
    @FXML
    public void initialize() {
        // Guard: page is admin-only
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("admin")) {
            setStatus("Access denied.", true);
            disableAllActions();
            return;
        }

        setupTable();
        loadOrders();
        setupSearch();
    }

    // =========================================================================
    // Table setup
    // =========================================================================

    private void setupTable() {
        colOrderNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrderNumber()));
        colCustomer.setCellValueFactory(c -> {
            User u = userRepo.findById(c.getValue().getUserId());
            String name = (u != null) ? u.getFirstName() + " " + u.getLastName() : c.getValue().getUserId();
            return new SimpleStringProperty(name);
        });
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().getDisplayName()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getTotal())));
        colDate.setCellValueFactory(c -> {
            if (c.getValue().getCreatedAt() != null)
                return new SimpleStringProperty(c.getValue().getCreatedAt().format(fmt));
            return new SimpleStringProperty("—");
        });

        setupActionsColumn();

        // Clicking a row loads it into the detail panel
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, old, next) -> {
            if (next != null)
                loadDetailPanel(next);
        });
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final HBox box = new HBox(viewBtn);
            {
                box.setAlignment(Pos.CENTER);
                viewBtn.getStyleClass().add("btn-edit");
                viewBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    loadDetailPanel(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadOrders() {
        allOrders.clear();
        List<Order> orders = orderService.getAllOrders();
        allOrders.addAll(orders);
        orderTable.setItems(allOrders);

        // Reload FIFO queue
        orderService.loadPendingQueue();
        refreshQueueBadge();

        setStatus("", false);
        clearDetailPanel();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, query) -> {
            if (query == null || query.isBlank()) {
                orderTable.setItems(allOrders);
                return;
            }
            String q = query.toLowerCase();
            ObservableList<Order> filtered = FXCollections.observableArrayList();
            for (Order o : allOrders) {
                boolean matchNum = o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(q);
                boolean matchStatus = o.getStatus() != null && o.getStatus().getDisplayName().toLowerCase().contains(q);
                User u = userRepo.findById(o.getUserId());
                boolean matchName = u != null &&
                        (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(q);
                if (matchNum || matchStatus || matchName)
                    filtered.add(o);
            }
            orderTable.setItems(filtered);
        });
    }

    // =========================================================================
    // Detail panel
    // =========================================================================

    private void loadDetailPanel(Order order) {
        selectedOrder = order;

        detailOrderNum.setText(order.getOrderNumber() != null ? order.getOrderNumber() : "—");

        User u = userRepo.findById(order.getUserId());
        if (u != null) {
            detailCustomer.setText(u.getFirstName() + " " + u.getLastName());
            detailEmail.setText(u.getEmail() != null ? u.getEmail() : "—");
        } else {
            detailCustomer.setText("Unknown");
            detailEmail.setText("—");
        }

        detailAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress() : "—");
        detailPayment.setText(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "—");
        detailTotal.setText(String.format("$%.2f", order.getTotal()));
        detailDate.setText(order.getCreatedAt() != null ? order.getCreatedAt().format(fmt) : "—");
        detailStatus.setText(order.getStatus() != null ? order.getStatus().getDisplayName() : "—");

        setStatus("", false);
        updateActionButtons();
    }

    private void clearDetailPanel() {
        selectedOrder = null;
        detailOrderNum.setText("—");
        detailCustomer.setText("—");
        detailEmail.setText("—");
        detailAddress.setText("—");
        detailPayment.setText("—");
        detailTotal.setText("—");
        detailDate.setText("—");
        detailStatus.setText("—");
        updateActionButtons();
    }

    private void refreshQueueBadge() {
        int size = orderService.getPendingQueueSize();
        queueSizeLabel.setText(size + " pending in queue");
        dequeueBtn.setDisable(orderService.isPendingQueueEmpty());
    }

    private void updateActionButtons() {
        boolean noOrder = (selectedOrder == null);
        markProcessingBtn.setDisable(noOrder);
        markShippedBtn.setDisable(noOrder);
        markDeliveredBtn.setDisable(noOrder);
    }

    private void disableAllActions() {
        dequeueBtn.setDisable(true);
        markProcessingBtn.setDisable(true);
        markShippedBtn.setDisable(true);
        markDeliveredBtn.setDisable(true);
    }

    // =========================================================================
    // Button handlers
    // =========================================================================

    /** Dequeue the oldest PENDING order from the FIFO queue and display it. */
    @FXML
    private void handleDequeue() {
        if (orderService.isPendingQueueEmpty()) {
            setStatus("No pending orders in the queue.", false);
            return;
        }
        Order dequeued = orderService.dequeue();
        if (dequeued == null) {
            setStatus("Queue is empty.", false);
            return;
        }
        // Refresh table so the status change is visible
        loadOrders();
        // Highlight the dequeued order in the table and detail panel
        orderTable.getItems().stream()
                .filter(o -> o.getId().equals(dequeued.getId()))
                .findFirst()
                .ifPresent(o -> {
                    orderTable.getSelectionModel().select(o);
                    loadDetailPanel(o);
                });
        setStatus("Order #" + dequeued.getOrderNumber() + " dequeued → Processing.", false);
    }

    @FXML
    private void handleMarkProcessing() {
        if (selectedOrder == null)
            return;
        orderService.updateStatus(selectedOrder, OrderStatus.PROCESSING);
        refreshAfterStatusChange("Marked as Processing.");
    }

    @FXML
    private void handleMarkShipped() {
        if (selectedOrder == null)
            return;

        // Async email fires inside markAsShipped — UI stays responsive
        orderService.markAsShipped(selectedOrder);

        Platform.runLater(() -> {
            refreshAfterStatusChange("Marked as Shipped. Shipping notification email sent.");
        });
    }

    @FXML
    private void handleMarkDelivered() {
        if (selectedOrder == null)
            return;
        orderService.updateStatus(selectedOrder, OrderStatus.DELIVERED);
        refreshAfterStatusChange("Marked as Delivered.");
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
        setStatus("Orders refreshed.", false);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void refreshAfterStatusChange(String message) {
        String currentId = selectedOrder != null ? selectedOrder.getId() : null;
        loadOrders();
        if (currentId != null) {
            orderTable.getItems().stream()
                    .filter(o -> o.getId().equals(currentId))
                    .findFirst()
                    .ifPresent(o -> {
                        orderTable.getSelectionModel().select(o);
                        loadDetailPanel(o);
                    });
        }
        setStatus(message, false);
    }

    private void setStatus(String message, boolean isError) {
        statusMessage.setText(message);
        statusMessage.setStyle(isError
                ? "-fx-text-fill: #f44336;"
                : "-fx-text-fill: #4caf50;");
    }
}
