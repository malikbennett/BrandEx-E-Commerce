package com.brandex.ui;

import com.brandex.datastructures.LinkedList;
import com.brandex.models.Order;
import com.brandex.models.User;
import com.brandex.service.OrderService;
import com.brandex.service.AuthService;

import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

// The controller class for handling the order history view.
public class OrderHistoryController {

    @FXML
    private VBox ordersContainer;

    // Initializes the order history view
    @FXML
    public void initialize() {
        populateOrders();
    }

    // Populates the orders container
    private void populateOrders() {
        ordersContainer.getChildren().clear();

        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        // Fetch user's historical orders via cache (load only once)
        if (!OrderService.getInstance().isLoaded())
            OrderService.getInstance().loadOrders();
        LinkedList<Order> orders = OrderService.getInstance().getOrdersByUserId(currentUser.getId());

        if (orders.isEmpty()) {
            Label noOrdersLabel = new Label("You haven't placed any orders yet. Time to go shopping!");
            noOrdersLabel.setStyle("-fx-font-size: 14; -fx-text-fill: -brand-text-dim;");
            ordersContainer.getChildren().add(noOrdersLabel);
            ordersContainer.setAlignment(Pos.CENTER);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        orders.traverse(order -> {
            Tile tile = new Tile();
            tile.getStyleClass().add("card-premium"); // Keep the design language
            tile.getStyleClass().add(Styles.ELEVATED_1);

            tile.setTitle("Order " + order.getOrderNumber());

            String dateFormatted = order.getCreatedAt() != null ? order.getCreatedAt().format(formatter)
                    : "Unknown Date";
            tile.setDescription("Placed on: " + dateFormatted + "  •  Status: " + order.getStatus().toString());

            Label priceLabel = new Label(String.format("$%.2f", order.getTotal()));
            priceLabel.getStyleClass().add(Styles.TEXT_BOLD);
            priceLabel.setStyle("-fx-font-size: 18; -fx-text-fill: -brand-primary;");

            VBox actionBox = new VBox(5);
            actionBox.setAlignment(Pos.CENTER_RIGHT);
            actionBox.getChildren().addAll(priceLabel);

            // Add the action Box configuration directly
            tile.setAction(actionBox);
            tile.setActionHandler(() -> {
                // Future expansion: open a detailed view showing OrderItems
                System.out.println("Clicked order: " + order.getOrderNumber());
            });

            ordersContainer.getChildren().add(tile);
        });
    }
}
