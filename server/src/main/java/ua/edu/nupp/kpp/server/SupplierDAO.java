package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public void add(Supplier s) throws SQLException {
        String sql = "INSERT INTO supplier(name,contact_email,lead_time_days,reliability) VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.executeUpdate();
        }
    }

    public List<Supplier> getAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT id,name,contact_email,lead_time_days,reliability FROM supplier ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Supplier getById(long id) throws SQLException {
        String sql = "SELECT id,name,contact_email,lead_time_days,reliability FROM supplier WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE supplier SET name=?,contact_email=?,lead_time_days=?,reliability=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.setLong(5, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM supplier WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Supplier s) throws SQLException {
        ps.setString(1, s.getName());
        ps.setString(2, s.getContactEmail());
        ps.setInt(3, s.getLeadTimeDays());
        ps.setDouble(4, s.getReliability());
    }

    private Supplier map(ResultSet r) throws SQLException {
        return new Supplier(
                r.getLong("id"),
                r.getString("name"),
                r.getString("contact_email"),
                r.getInt("lead_time_days"),
                r.getDouble("reliability"));
    }
}
