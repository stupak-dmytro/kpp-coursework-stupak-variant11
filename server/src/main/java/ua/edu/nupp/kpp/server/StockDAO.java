package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    // recorded_at не передається: значення формує MySQL (DEFAULT CURRENT_TIMESTAMP)
    public void add(Stock s) throws SQLException {
        String sql = "INSERT INTO stock(product_id,quantity,min_level,daily_consumption) VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.executeUpdate();
        }
    }

    public List<Stock> getAll() throws SQLException {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT id,product_id,quantity,min_level,daily_consumption,recorded_at FROM stock ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Stock getById(long id) throws SQLException {
        String sql = "SELECT id,product_id,quantity,min_level,daily_consumption,recorded_at FROM stock WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    // Дата фіксації recorded_at під час редагування не змінюється
    public void update(Stock s) throws SQLException {
        String sql = "UPDATE stock SET product_id=?,quantity=?,min_level=?,daily_consumption=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.setLong(5, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM stock WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Stock s) throws SQLException {
        ps.setLong(1, s.getProductId());
        ps.setDouble(2, s.getQuantity());
        ps.setDouble(3, s.getMinLevel());
        ps.setDouble(4, s.getDailyConsumption());
    }

    private Stock map(ResultSet r) throws SQLException {
        return new Stock(
                r.getLong("id"),
                r.getLong("product_id"),
                r.getDouble("quantity"),
                r.getDouble("min_level"),
                r.getDouble("daily_consumption"),
                r.getString("recorded_at"));
    }
}
