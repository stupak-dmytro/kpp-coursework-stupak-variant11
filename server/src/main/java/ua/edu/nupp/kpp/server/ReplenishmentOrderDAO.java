package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReplenishmentOrderDAO {

    // created_at не передається: значення формує MySQL (DEFAULT CURRENT_TIMESTAMP)
    public void add(ReplenishmentOrder o) throws SQLException {
        String sql = "INSERT INTO replenishment_order"
                + "(stock_id,supplier_id,ordered_quantity,state,f1,f2,f3,priority_index) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, o);
            ps.executeUpdate();
        }
    }

    public List<ReplenishmentOrder> getAll() throws SQLException {
        List<ReplenishmentOrder> list = new ArrayList<>();
        String sql = "SELECT id,stock_id,supplier_id,ordered_quantity,state,f1,f2,f3,priority_index,created_at "
                + "FROM replenishment_order ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public ReplenishmentOrder getById(long id) throws SQLException {
        String sql = "SELECT id,stock_id,supplier_id,ordered_quantity,state,f1,f2,f3,priority_index,created_at "
                + "FROM replenishment_order WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    // Дата створення created_at під час редагування не змінюється
    public void update(ReplenishmentOrder o) throws SQLException {
        String sql = "UPDATE replenishment_order SET "
                + "stock_id=?,supplier_id=?,ordered_quantity=?,state=?,f1=?,f2=?,f3=?,priority_index=? "
                + "WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, o);
            ps.setLong(9, o.getId());
            ps.executeUpdate();
        }
    }

    // Лише записує стан у БД; чи дозволений перехід, перевірятиме Service
    public void updateState(long id, OrderState state) throws SQLException {
        String sql = "UPDATE replenishment_order SET state=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, state.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM replenishment_order WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, ReplenishmentOrder o) throws SQLException {
        ps.setLong(1, o.getStockId());
        ps.setLong(2, o.getSupplierId());
        ps.setDouble(3, o.getOrderedQuantity());
        ps.setString(4, o.getState().name());
        ps.setDouble(5, o.getF1());
        ps.setDouble(6, o.getF2());
        ps.setDouble(7, o.getF3());
        ps.setDouble(8, o.getPriorityIndex());
    }

    private ReplenishmentOrder map(ResultSet r) throws SQLException {
        return new ReplenishmentOrder(
                r.getLong("id"),
                r.getLong("stock_id"),
                r.getLong("supplier_id"),
                r.getDouble("ordered_quantity"),
                OrderState.valueOf(r.getString("state")),
                r.getDouble("f1"),
                r.getDouble("f2"),
                r.getDouble("f3"),
                r.getDouble("priority_index"),
                r.getString("created_at"));
    }
}
