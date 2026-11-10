package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpWarehouseClient {

    private static final String BASE = "http://localhost:8080/warehouse-replenishment-server";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- Товари ----------

    public List<Product> getProducts() {
        return Arrays.asList(read(get("/products"), Product[].class));
    }

    public void addProduct(Product p) {
        post("/products", params("add", "sku", p.getSku(), "name", p.getName(), "unit", p.getUnit()));
    }

    public void updateProduct(Product p) {
        post("/products", params("update", "id", String.valueOf(p.getId()),
                "sku", p.getSku(), "name", p.getName(), "unit", p.getUnit()));
    }

    public void deleteProduct(long id) {
        post("/products", params("delete", "id", String.valueOf(id)));
    }

    // ---------- Постачальники ----------

    public List<Supplier> getSuppliers() {
        return Arrays.asList(read(get("/suppliers"), Supplier[].class));
    }

    public void addSupplier(Supplier s) {
        post("/suppliers", supplierParams("add", s));
    }

    public void updateSupplier(Supplier s) {
        Map<String, String> p = supplierParams("update", s);
        p.put("id", String.valueOf(s.getId()));
        post("/suppliers", p);
    }

    public void deleteSupplier(long id) {
        post("/suppliers", params("delete", "id", String.valueOf(id)));
    }

    // ---------- Запаси ----------

    public List<Stock> getStocks() {
        return Arrays.asList(read(get("/stocks"), Stock[].class));
    }

    public void addStock(Stock s) {
        post("/stocks", stockParams("add", s));
    }

    public void updateStock(Stock s) {
        Map<String, String> p = stockParams("update", s);
        p.put("id", String.valueOf(s.getId()));
        post("/stocks", p);
    }

    public void deleteStock(long id) {
        post("/stocks", params("delete", "id", String.valueOf(id)));
    }

    // ---------- Замовлення поповнення ----------

    public List<ReplenishmentOrder> getOrders() {
        return Arrays.asList(read(get("/orders"), ReplenishmentOrder[].class));
    }

    // Клієнт передає лише запас, постачальника та кількість; F1, F2, F3 і I рахує сервер
    public void addOrder(long stockId, long supplierId, double orderedQuantity) {
        post("/orders", params("add", "stockId", String.valueOf(stockId),
                "supplierId", String.valueOf(supplierId),
                "orderedQuantity", String.valueOf(orderedQuantity)));
    }

    public void updateOrder(long id, long stockId, long supplierId, double orderedQuantity) {
        post("/orders", params("update", "id", String.valueOf(id),
                "stockId", String.valueOf(stockId),
                "supplierId", String.valueOf(supplierId),
                "orderedQuantity", String.valueOf(orderedQuantity)));
    }

    // Допустимість переходу перевіряє сервер; клієнт лише передає вибраний стан
    public void changeOrderState(long id, OrderState state) {
        post("/orders", params("transition", "id", String.valueOf(id), "state", state.name()));
    }

    public void deleteOrder(long id) {
        post("/orders", params("delete", "id", String.valueOf(id)));
    }

    // ---------- Допоміжні методи формування параметрів ----------

    private Map<String, String> supplierParams(String action, Supplier s) {
        return params(action, "name", s.getName(), "contactEmail", s.getContactEmail(),
                "leadTimeDays", String.valueOf(s.getLeadTimeDays()),
                "reliability", String.valueOf(s.getReliability()));
    }

    private Map<String, String> stockParams(String action, Stock s) {
        return params(action, "productId", String.valueOf(s.getProductId()),
                "quantity", String.valueOf(s.getQuantity()),
                "minLevel", String.valueOf(s.getMinLevel()),
                "dailyConsumption", String.valueOf(s.getDailyConsumption()));
    }

    private static Map<String, String> params(String action, String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("action", action);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    // ---------- HTTP ----------

    private String get(String path) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return send(req);
    }

    // Параметри передаються як application/x-www-form-urlencoded, тому Servlet читає їх через req.getParameter(...)
    private String post(String path, Map<String, String> params) {
        String form = params.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(Collectors.joining("&"));
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        return send(req);
    }

    private String send(HttpRequest req) {
        HttpResponse<String> response;
        try {
            response = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося з'єднатися із сервером: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запит до сервера перервано", e);
        }
        if (response.statusCode() >= 400) {
            throw new RuntimeException(errorText(response.body()));
        }
        return response.body();
    }

    // Сервер повертає помилку у вигляді {"error": "..."}: користувачу показуємо лише текст помилки
    private String errorText(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node != null && node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (Exception ignored) {
            // тіло відповіді не є JSON: повертаємо його як є
        }
        return body == null || body.isBlank() ? "Сервер повернув помилку" : body;
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Помилка розбору JSON-відповіді сервера", e);
        }
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
