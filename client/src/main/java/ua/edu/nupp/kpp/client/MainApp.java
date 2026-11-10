package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    // Уся робота з даними виконується лише через HTTP-клієнт
    private final HttpWarehouseClient api = new HttpWarehouseClient();

    private final TableView<Product> products = new TableView<>();
    private final TableView<Supplier> suppliers = new TableView<>();
    private final TableView<Stock> stocks = new TableView<>();
    private final TableView<ReplenishmentOrder> orders = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Товари", productPane()),
                new Tab("Постачальники", supplierPane()),
                new Tab("Запаси", stockPane()),
                new Tab("Замовлення поповнення", orderPane())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));

        stage.setTitle("Склад: поповнення запасів");
        stage.setScene(new Scene(tabs, 1150, 650));
        stage.show();
        refreshAll();
    }

    // ---------- Вкладка «Товари» ----------

    private Pane productPane() {
        columns(products, "ID", "id", "Код", "sku", "Назва", "name", "Одиниця", "unit");

        TextField sku = new TextField();
        TextField name = new TextField();
        TextField unit = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addProduct(new Product(null, sku.getText(), name.getText(), unit.getText()));
            clear(sku, name, unit);
            refreshProducts();
        }));
        update.setOnAction(e -> safe(() -> {
            Product selected = selected(products);
            api.updateProduct(new Product(selected.getId(), sku.getText(), name.getText(), unit.getText()));
            refreshProducts();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteProduct(selected(products).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshProducts());

        products.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
            if (value != null) {
                sku.setText(value.getSku());
                name.setText(value.getName());
                unit.setText(value.getUnit());
            }
        });

        return layout(products, form(
                label("Код товару"), sku,
                label("Назва"), name,
                label("Одиниця виміру"), unit,
                new HBox(8, add, update, delete, refresh)));
    }

    // ---------- Вкладка «Постачальники» ----------

    private Pane supplierPane() {
        columns(suppliers, "ID", "id", "Назва", "name", "E-mail", "contactEmail",
                "Термін постачання, днів", "leadTimeDays", "Надійність (0–100)", "reliability");

        TextField name = new TextField();
        TextField email = new TextField();
        TextField leadTime = new TextField();
        TextField reliability = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addSupplier(readSupplier(null, name, email, leadTime, reliability));
            clear(name, email, leadTime, reliability);
            refreshSuppliers();
        }));
        update.setOnAction(e -> safe(() -> {
            Supplier selected = selected(suppliers);
            api.updateSupplier(readSupplier(selected.getId(), name, email, leadTime, reliability));
            refreshSuppliers();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteSupplier(selected(suppliers).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshSuppliers());

        suppliers.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
            if (value != null) {
                name.setText(value.getName());
                email.setText(value.getContactEmail());
                leadTime.setText(String.valueOf(value.getLeadTimeDays()));
                reliability.setText(String.valueOf(value.getReliability()));
            }
        });

        return layout(suppliers, form(
                label("Назва"), name,
                label("Контактна пошта"), email,
                label("Термін постачання, днів"), leadTime,
                label("Надійність (0–100)"), reliability,
                new HBox(8, add, update, delete, refresh)));
    }

    // ---------- Вкладка «Запаси» ----------

    private Pane stockPane() {
        columns(stocks, "ID", "id", "ID товару", "productId", "Залишок", "quantity",
                "Мінімальний рівень", "minLevel", "Споживання за добу", "dailyConsumption",
                "Дата фіксації", "recordedAt");

        TextField productId = new TextField();
        TextField quantity = new TextField();
        TextField minLevel = new TextField();
        TextField dailyConsumption = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        Button importFirst = new Button("Імпорт першого рядка CSV (тест)");

        add.setOnAction(e -> safe(() -> {
            api.addStock(readStock(null, productId, quantity, minLevel, dailyConsumption));
            clear(productId, quantity, minLevel, dailyConsumption);
            refreshStocks();
        }));
        update.setOnAction(e -> safe(() -> {
            Stock selected = selected(stocks);
            api.updateStock(readStock(selected.getId(), productId, quantity, minLevel, dailyConsumption));
            refreshStocks();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteStock(selected(stocks).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshStocks());

        // Початок CSV-імпорту: один рядок файлу -> клієнтська модель -> HTTP -> сервер
        importFirst.setOnAction(e -> safe(() -> {
            Stock fromCsv = CsvStockReader.readFirstRow(Path.of("sample-data", "stock.csv"));
            api.addStock(fromCsv);
            refreshStocks();
        }));

        stocks.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
            if (value != null) {
                productId.setText(String.valueOf(value.getProductId()));
                quantity.setText(String.valueOf(value.getQuantity()));
                minLevel.setText(String.valueOf(value.getMinLevel()));
                dailyConsumption.setText(String.valueOf(value.getDailyConsumption()));
            }
        });

        return layout(stocks, form(
                label("ID товару"), productId,
                label("Залишок"), quantity,
                label("Мінімальний рівень (більше 0)"), minLevel,
                label("Споживання за добу"), dailyConsumption,
                new HBox(8, add, update, delete, refresh),
                importFirst));
    }

    // ---------- Вкладка «Замовлення поповнення» ----------

    private Pane orderPane() {
        columns(orders, "ID", "id", "ID запасу", "stockId", "ID постачальника", "supplierId",
                "Кількість", "orderedQuantity", "Стан", "state",
                "F1", "f1", "F2", "f2", "F3", "f3", "I", "priorityIndex",
                "Створено", "createdAt");

        TextField stockId = new TextField();
        TextField supplierId = new TextField();
        TextField orderedQuantity = new TextField();
        ComboBox<OrderState> state = new ComboBox<>(FXCollections.observableArrayList(OrderState.values()));
        Button add = new Button("Створити");
        Button update = new Button("Перерахувати");
        Button change = new Button("Змінити стан");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        // F1, F2, F3 та I користувач не вводить: їх розраховує сервер
        add.setOnAction(e -> safe(() -> {
            api.addOrder(parseLong(stockId, "ID запасу"), parseLong(supplierId, "ID постачальника"),
                    parseDouble(orderedQuantity, "Кількість"));
            clear(stockId, supplierId, orderedQuantity);
            refreshOrders();
        }));
        update.setOnAction(e -> safe(() -> {
            ReplenishmentOrder selected = selected(orders);
            api.updateOrder(selected.getId(), parseLong(stockId, "ID запасу"),
                    parseLong(supplierId, "ID постачальника"), parseDouble(orderedQuantity, "Кількість"));
            refreshOrders();
        }));
        change.setOnAction(e -> safe(() -> {
            ReplenishmentOrder selected = selected(orders);
            OrderState newState = state.getValue();
            if (newState == null) {
                throw new IllegalArgumentException("Виберіть новий стан");
            }
            api.changeOrderState(selected.getId(), newState);
            refreshOrders();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteOrder(selected(orders).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshOrders());

        orders.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
            if (value != null) {
                stockId.setText(String.valueOf(value.getStockId()));
                supplierId.setText(String.valueOf(value.getSupplierId()));
                orderedQuantity.setText(String.valueOf(value.getOrderedQuantity()));
                state.setValue(value.getState());
            }
        });

        return layout(orders, form(
                label("ID запасу"), stockId,
                label("ID постачальника"), supplierId,
                label("Замовлена кількість"), orderedQuantity,
                label("Новий стан"), state,
                new HBox(8, add, update),
                new HBox(8, change, delete, refresh)));
    }

    // ---------- Читання полів форм ----------

    private Supplier readSupplier(Long id, TextField name, TextField email,
                                  TextField leadTime, TextField reliability) {
        return new Supplier(id, name.getText(), email.getText(),
                parseInt(leadTime, "Термін постачання"), parseDouble(reliability, "Надійність"));
    }

    private Stock readStock(Long id, TextField productId, TextField quantity,
                            TextField minLevel, TextField dailyConsumption) {
        return new Stock(id, parseLong(productId, "ID товару"), parseDouble(quantity, "Залишок"),
                parseDouble(minLevel, "Мінімальний рівень"),
                parseDouble(dailyConsumption, "Споживання за добу"), null);
    }

    private long parseLong(TextField field, String name) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректне значення поля «" + name + "»");
        }
    }

    private int parseInt(TextField field, String name) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректне значення поля «" + name + "»");
        }
    }

    // Приймає і крапку, і кому як десятковий роздільник
    private double parseDouble(TextField field, String name) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректне значення поля «" + name + "»");
        }
    }

    // ---------- Оновлення даних з сервера ----------

    private void refreshAll() {
        safe(() -> {
            products.setItems(FXCollections.observableArrayList(api.getProducts()));
            suppliers.setItems(FXCollections.observableArrayList(api.getSuppliers()));
            stocks.setItems(FXCollections.observableArrayList(api.getStocks()));
            orders.setItems(FXCollections.observableArrayList(api.getOrders()));
        });
    }

    private void refreshProducts() {
        safe(() -> products.setItems(FXCollections.observableArrayList(api.getProducts())));
    }

    private void refreshSuppliers() {
        safe(() -> suppliers.setItems(FXCollections.observableArrayList(api.getSuppliers())));
    }

    private void refreshStocks() {
        safe(() -> stocks.setItems(FXCollections.observableArrayList(api.getStocks())));
    }

    private void refreshOrders() {
        safe(() -> orders.setItems(FXCollections.observableArrayList(api.getOrders())));
    }

    // ---------- Допоміжні методи інтерфейсу ----------

    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть запис");
        }
        return value;
    }

    // Будь-яка помилка перетворюється на стандартне вікно, а не на stack trace
    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(String.valueOf(e.getMessage()));
            showError(e);
        }
    }

    private void showError(Throwable e) {
        String message = e == null || e.getMessage() == null ? "Помилка" : e.getMessage();
        Alert dialog = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        dialog.setHeaderText(null);
        dialog.showAndWait();
    }

    private <S> void columns(TableView<S> table, String... titlesAndProperties) {
        table.getColumns().clear();
        for (int i = 0; i + 1 < titlesAndProperties.length; i += 2) {
            TableColumn<S, Object> column = new TableColumn<>(titlesAndProperties[i]);
            column.setCellValueFactory(new PropertyValueFactory<>(titlesAndProperties[i + 1]));
            table.getColumns().add(column);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private Pane layout(TableView<?> table, Pane form) {
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.68);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private VBox form(Node... nodes) {
        VBox box = new VBox(8, nodes);
        box.setPadding(new Insets(10));
        return box;
    }

    private Label label(String text) {
        return new Label(text);
    }

    private void clear(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
