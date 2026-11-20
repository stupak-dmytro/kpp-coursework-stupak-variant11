package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Перевірки виконуються до звернення до DAO, тому для цих тестів MySQL не потрібен
class ServiceValidationTest {

    private final ProductService productService = new ProductService();
    private final SupplierService supplierService = new SupplierService();
    private final StockService stockService = new StockService();
    private final ReplenishmentOrderService orderService = new ReplenishmentOrderService();

    @Test
    void productWithEmptySkuIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> productService.add(new Product(null, "", "Товар", "шт")));
        assertEquals("Усі поля товару є обов'язковими", e.getMessage());
    }

    @Test
    void productWithBlankNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.add(new Product(null, "X-1", "   ", "шт")));
    }

    @Test
    void productUpdateWithEmptyUnitIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.update(new Product(1L, "X-1", "Товар", "")));
    }

    @Test
    void supplierWithoutNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> supplierService.add(new Supplier(null, "", "a@b.example", 3, 80.0)));
    }

    @Test
    void supplierWithNegativeLeadTimeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> supplierService.add(new Supplier(null, "Тест", "a@b.example", -1, 80.0)));
    }

    @Test
    void supplierReliabilityMustBeBetween0And100() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> supplierService.add(new Supplier(null, "Тест", "a@b.example", 3, 120.0)));
        assertEquals("Надійність постачальника має бути в діапазоні 0..100", e.getMessage());
    }

    @Test
    void stockWithNegativeQuantityIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> stockService.add(new Stock(null, 1L, -5.0, 100.0, 5.0, null)));
        assertTrue(e.getMessage().contains("Некоректні числові дані"));
    }

    @Test
    void stockMinLevelMustBeGreaterThanZero() {
        assertThrows(IllegalArgumentException.class,
                () -> stockService.add(new Stock(null, 1L, 10.0, 0.0, 5.0, null)));
    }

    @Test
    void stockWithoutProductIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> stockService.add(new Stock(null, 0L, 10.0, 100.0, 5.0, null)));
        assertEquals("Некоректний товар у записі запасу", e.getMessage());
    }

    @Test
    void orderWithZeroQuantityIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> orderService.add(1L, 1L, 0.0));
        assertTrue(e.getMessage().contains("Некоректні дані замовлення"));
    }

    @Test
    void orderWithoutStockOrSupplierIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> orderService.add(0L, 1L, 10.0));
        assertThrows(IllegalArgumentException.class, () -> orderService.add(1L, 0L, 10.0));
    }
}
