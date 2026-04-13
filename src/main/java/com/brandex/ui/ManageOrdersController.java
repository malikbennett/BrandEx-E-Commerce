package com.brandex.ui;

import com.brandex.models.Order;
import com.brandex.models.User;
import com.brandex.models.enums.OrderStatus;
import com.brandex.service.UserService;
import com.brandex.service.AuthService;
import com.brandex.service.OrderService;
import com.brandex.utilities.StatusLabelHelper;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

// The controller class for handling the manage orders view.
public class ManageOrdersController {

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

    @FXML
    private Label queueSizeLabel;

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

    @FXML
    private Button dequeueBtn;
    @FXML
    private Button markProcessingBtn;
    @FXML
    private Button markShippedBtn;
    @FXML
    private Button markDeliveredBtn;

    @FXML
    private TextField searchField;

    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();
    private final OrderService orderService = OrderService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private Order selectedOrder = null;

    // Initializes the manage orders view
    @FXML
    public void initialize() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("admin")) {
            StatusLabelHelper.showError(statusMessage, "Access denied.");
            disableAllActions();
            return;
        }

        setupTable();
        loadOrders();
        setupSearch();
    }

    // Sets up the table
    private void setupTable() {
        colOrderNum.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderNumber()));
        colCustomer.setCellValueFactory(cellData -> {
            User user = userService.searchById(cellData.getValue().getUserId());
            return new SimpleStringProperty(user != null ? user.getFullName() : cellData.getValue().getUserId());
        });
        colStatus.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        colTotal.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotal())));
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null)
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            return new SimpleStringProperty("—");
        });

        setupActionsColumn();
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, old, next) -> {
            if (next != null)
                loadDetailPanel(next);
        });
    }

    // Sets up the actions column
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

    // Loads the orders
    private void loadOrders() {
        try {
            if (!userService.isLoaded())
                userService.loadUsers();
            if (!orderService.isLoaded())
                orderService.loadOrders();

            allOrders.clear();
            orderService.forEachOrder(order -> this.allOrders.add(order));
            orderTable.setItems(allOrders);

            orderService.loadPendingQueue();
            refreshQueueBadge();

            StatusLabelHelper.clear(statusMessage);
            clearDetailPanel();
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(statusMessage, "Database error: Could not load orders.");
        }
    }

    // Sets up the search
    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                orderTable.setItems(allOrders);
                return;
            }
            String query = val.toLowerCase();
            ObservableList<Order> filtered = FXCollections.observableArrayList();
            for (Order order : allOrders) {
                User user = userService.searchById(order.getUserId());
                if (order.getOrderNumber().toLowerCase().contains(query) ||
                        order.getStatus().getDisplayName().toLowerCase().contains(query) ||
                        user.getFullName().toLowerCase().contains(query))
                    filtered.add(order);
            }
            orderTable.setItems(filtered);
        });
    }

    // Loads the detail panel
    private void loadDetailPanel(Order order) {
        selectedOrder = order;

        detailOrderNum.setText(order.getOrderNumber() != null ? order.getOrderNumber() : "—");

        User user = userService.searchById(order.getUserId());
        if (user != null) {
            detailCustomer.setText(user.getFullName());
            detailEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
        } else {
            detailCustomer.setText("Unknown");
            detailEmail.setText("—");
        }

        detailAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress() : "—");
        detailPayment.setText(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "—");
        detailTotal.setText(String.format("$%.2f", order.getTotal()));
        detailDate.setText(order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "—");
        detailStatus.setText(order.getStatus() != null ? order.getStatus().getDisplayName() : "—");

        StatusLabelHelper.clear(statusMessage);
        updateActionButtons();
    }

    // Clears the detail panel
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

    // Refreshes the queue badge
    private void refreshQueueBadge() {
        int size = orderService.getPendingQueueSize();
        queueSizeLabel.setText(size + " pending in queue");
        dequeueBtn.setDisable(orderService.isPendingQueueEmpty());
    }

    // Updates the action buttons
    private void updateActionButtons() {
        boolean noOrder = (selectedOrder == null);
        markProcessingBtn.setDisable(noOrder);
        markShippedBtn.setDisable(noOrder);
        markDeliveredBtn.setDisable(noOrder);
    }

    // Disables all actions
    private void disableAllActions() {
        dequeueBtn.setDisable(true);
        markProcessingBtn.setDisable(true);
        markShippedBtn.setDisable(true);
        markDeliveredBtn.setDisable(true);
    }

    // Dequeues the next order
    @FXML
    private void handleDequeue() {
        if (orderService.isPendingQueueEmpty()) {
            StatusLabelHelper.showSuccess(statusMessage, "No pending orders in the queue.");
            return;
        }
        try {
            Order dequeued = orderService.dequeue();
            if (dequeued == null) {
                StatusLabelHelper.showSuccess(statusMessage, "Queue is empty.");
                return;
            }
            // Refresh table so the status change is visible
            orderService.reloadOrders();
            loadOrders();
            // Highlight the dequeued order in the table and detail panel
            orderTable.getItems().stream()
                    .filter(o -> o.getId().equals(dequeued.getId()))
                    .findFirst()
                    .ifPresent(o -> {
                        orderTable.getSelectionModel().select(o);
                        loadDetailPanel(o);
                    });
            StatusLabelHelper.showSuccess(statusMessage,
                    "Order #" + dequeued.getOrderNumber() + " dequeued → Processing.");
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(statusMessage, "Database error: Failed to dequeue order.");
        }
    }

    // Marks the order as processing
    @FXML
    private void handleMarkProcessing() {
        if (selectedOrder == null)
            return;
        try {
            orderService.updateStatus(selectedOrder, OrderStatus.PROCESSING);
            refreshAfterStatusChange("Marked as Processing.");
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(statusMessage, "Database error: Failed to update status.");
        }
    }

    // Marks the order as shipped
    @FXML
    private void handleMarkShipped() {
        if (selectedOrder == null)
            return;

        orderService.markAsShipped(selectedOrder, () -> {
            Platform.runLater(
                    () -> StatusLabelHelper.showSuccess(statusMessage, "Order Shipped. Notification email sent."));
        }, error -> {
            Platform.runLater(() -> StatusLabelHelper.showError(statusMessage, "Shipped, but EMAIL FAILED: " + error));
        });

        refreshAfterStatusChange("Updating order status...");
    }

    // Marks the order as delivered
    @FXML
    private void handleMarkDelivered() {
        if (selectedOrder == null)
            return;

        orderService.updateStatus(selectedOrder, OrderStatus.DELIVERED, () -> {
            Platform.runLater(
                    () -> StatusLabelHelper.showSuccess(statusMessage, "Order Delivered. Confirmation email sent."));
        }, error -> {
            Platform.runLater(
                    () -> StatusLabelHelper.showError(statusMessage, "Delivered, but EMAIL FAILED: " + error));
        });

        refreshAfterStatusChange("Updating order status...");
    }

    // Refreshes the orders
    @FXML
    private void handleRefresh() {
        try {
            orderService.reloadOrders();
            loadOrders();
            StatusLabelHelper.showSuccess(statusMessage, "Orders refreshed.");
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(statusMessage, "Database error: Could not refresh orders.");
        }
    }

    // Refreshes after a status change
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
        StatusLabelHelper.showSuccess(statusMessage, message);
    }
}
