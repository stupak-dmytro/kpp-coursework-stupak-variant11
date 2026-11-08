package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ReplenishmentOrderService {

    private static final Logger LOG = Logger.getLogger(ReplenishmentOrderService.class.getName());

    private final ReplenishmentOrderDAO dao = new ReplenishmentOrderDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<ReplenishmentOrder> getAll() throws SQLException {
        return dao.getAll();
    }

    // Користувач передає лише запас, постачальника та кількість; F1, F2, F3 та I рахує сервер
    public void add(long stockId, long supplierId, double orderedQuantity) throws SQLException {
        ReplenishmentOrder order = build(null, stockId, supplierId, orderedQuantity, OrderState.CREATED);
        dao.add(order);
        LOG.info("Створено замовлення поповнення");
    }

    // Під час редагування показники перераховуються, а поточний стан замовлення не змінюється
    public void update(long id, long stockId, long supplierId, double orderedQuantity) throws SQLException {
        ReplenishmentOrder existing = dao.getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Замовлення поповнення не знайдено");
        }
        ReplenishmentOrder order = build(id, stockId, supplierId, orderedQuantity, existing.getState());
        dao.update(order);
        LOG.info("Оновлено замовлення поповнення");
    }

    // Дозволено лише послідовні переходи: CREATED -> APPROVED -> ORDERED -> RECEIVED
    public static boolean isAllowedTransition(OrderState from, OrderState to) {
        return (from == OrderState.CREATED && to == OrderState.APPROVED)
                || (from == OrderState.APPROVED && to == OrderState.ORDERED)
                || (from == OrderState.ORDERED && to == OrderState.RECEIVED);
    }

    // Зміна стану життєвого циклу: правило переходу перевіряє Service, а DAO лише записує новий стан
    public void changeState(long id, OrderState next) throws SQLException {
        ReplenishmentOrder current = dao.getById(id);
        if (current == null) {
            throw new IllegalArgumentException("Замовлення поповнення не знайдено");
        }
        if (!isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану: " + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан: " + current.getState() + " -> " + next);
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено замовлення поповнення");
    }

    private ReplenishmentOrder build(Long id, long stockId, long supplierId,
                                     double orderedQuantity, OrderState state) throws SQLException {
        validate(stockId, supplierId, orderedQuantity);
        Stock stock = requireStock(stockId);
        Supplier supplier = requireSupplier(supplierId);

        double f1 = RiskCalculator.factor1(stock.getQuantity(), stock.getMinLevel());
        double f2 = RiskCalculator.factor2(stock.getDailyConsumption(), stock.getMinLevel());
        double f3 = RiskCalculator.factor3(supplier.getLeadTimeDays(), supplier.getReliability());
        double priorityIndex = RiskCalculator.calculate(f1, f2, f3);

        return new ReplenishmentOrder(id, stockId, supplierId, orderedQuantity, state,
                f1, f2, f3, priorityIndex, null);
    }

    private void validate(long stockId, long supplierId, double orderedQuantity) {
        if (stockId <= 0 || supplierId <= 0 || !(orderedQuantity > 0)) {
            throw new IllegalArgumentException(
                    "Некоректні дані замовлення поповнення: потрібні запас, постачальник і додатна кількість");
        }
    }

    private Stock requireStock(long id) throws SQLException {
        Stock stock = stockDAO.getById(id);
        if (stock == null) {
            throw new IllegalArgumentException("Запас не знайдено");
        }
        return stock;
    }

    private Supplier requireSupplier(long id) throws SQLException {
        Supplier supplier = supplierDAO.getById(id);
        if (supplier == null) {
            throw new IllegalArgumentException("Постачальника не знайдено");
        }
        return supplier;
    }
}
