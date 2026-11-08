package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class SupplierService {

    private static final Logger LOG = Logger.getLogger(SupplierService.class.getName());

    private final SupplierDAO dao = new SupplierDAO();

    public List<Supplier> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(Supplier s) throws SQLException {
        validate(s);
        dao.add(s);
        LOG.info("Додано постачальника");
    }

    public void update(Supplier s) throws SQLException {
        validate(s);
        dao.update(s);
        LOG.info("Оновлено постачальника");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено постачальника");
    }

    private void validate(Supplier s) {
        if (s == null || blank(s.getName()) || blank(s.getContactEmail())) {
            throw new IllegalArgumentException("Назва та контактна пошта постачальника є обов'язковими");
        }
        if (s.getLeadTimeDays() < 0) {
            throw new IllegalArgumentException("Термін постачання не може бути від'ємним");
        }
        // Запис через "!(...)" відхиляє також значення NaN
        if (!(s.getReliability() >= 0 && s.getReliability() <= 100)) {
            throw new IllegalArgumentException("Надійність постачальника має бути в діапазоні 0..100");
        }
    }

    private boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
