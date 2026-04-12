package com.brandex.ui;

import com.brandex.models.User;
import com.brandex.service.AuthService;
import com.brandex.service.UserService;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.brandex.App;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.format.DateTimeFormatter;

public class ManageUsersController {
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> idColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> phoneColumn;
    @FXML
    private TableColumn<User, String> addressColumn;
    @FXML
    private TableColumn<User, String> joinedDateColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private javafx.scene.control.TextField searchField;

    private final UserService userService = UserService.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final ObservableList<User> allUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        setupSearch();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : "N/A"));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
        addressColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShippingAddress()));
        joinedDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("N/A");
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final HBox container = new HBox(10, editBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("btn-edit");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadUsers() {
        if (AuthService.getInstance().getCurrentUser().getRole().equalsIgnoreCase("admin")
                && userService.getUsersTree().isEmpty()) {
            userService.loadUsers();
        }

        allUsers.clear();
        userService.forEachUser(allUsers::add);
        userTable.setItems(allUsers);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                userTable.setItems(allUsers);
                return;
            }
            String query = val.toLowerCase();
            ObservableList<User> filtered = FXCollections.observableArrayList();
            for (User u : allUsers) {
                if ((u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(query)
                        || u.getUsername().toLowerCase().contains(query)
                        || u.getEmail().toLowerCase().contains(query)) {
                    filtered.add(u);
                }
            }
            userTable.setItems(filtered);
        });
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("fxml/admin/EditUserDialog.fxml"));
            VBox root = loader.load();

            EditUserDialogController controller = loader.getController();
            controller.setUser(user);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User - " + user.getUsername());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            // Get the stage from the table's scene
            dialogStage.initOwner(userTable.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadUsers(); // Refresh the table
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening edit dialog: " + e.getMessage());
        }
    }
}
