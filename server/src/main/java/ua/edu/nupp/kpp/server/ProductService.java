package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ProductService {

    private static final Logger LOG = Logger.getLogger(ProductService.class.getName());

    private final ProductDAO dao = new ProductDAO();

    public List<Product> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Product p) throws SQLException {
        validate(p);
        dao.add(p);
        LOG.info("Додано товар");
    }

    public void update(Product p) throws SQLException {
        validate(p);
        dao.update(p);
        LOG.info("Оновлено товар");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено товар");
    }

    private void validate(Product p) {
        if (p == null || blank(p.getSku()) || blank(p.getName()) || blank(p.getUnit())) {
            throw new IllegalArgumentException("Усі поля товару є обов'язковими");
        }
    }

    private boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
