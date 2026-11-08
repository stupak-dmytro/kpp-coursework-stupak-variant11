package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class StockService {

    private static final Logger LOG = Logger.getLogger(StockService.class.getName());

    private final StockDAO dao = new StockDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public List<Stock> getAll() throws SQLException {
        return dao.getAll();
    }

    public Stock getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Stock s) throws SQLException {
        validate(s);
        requireProduct(s.getProductId());
        dao.add(s);
        LOG.info("Додано запис запасу");
    }

    public void update(Stock s) throws SQLException {
        validate(s);
        requireProduct(s.getProductId());
        dao.update(s);
        LOG.info("Оновлено запис запасу");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено запис запасу");
    }

    private void validate(Stock s) {
        if (s == null || s.getProductId() <= 0) {
            throw new IllegalArgumentException("Некоректний товар у записі запасу");
        }
        // Запис через "!(...)" відхиляє також значення NaN
        if (!(s.getQuantity() >= 0)
                || !(s.getMinLevel() > 0)
                || !(s.getDailyConsumption() >= 0)) {
            throw new IllegalArgumentException(
                    "Некоректні числові дані запасу: залишок і споживання не можуть бути від'ємними, "
                            + "мінімальний рівень має бути більшим за 0");
        }
    }

    private void requireProduct(long id) throws SQLException {
        if (productDAO.getById(id) == null) {
            throw new IllegalArgumentException("Товар не знайдено");
        }
    }
}
