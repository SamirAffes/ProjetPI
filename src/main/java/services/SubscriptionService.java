package services;

import entities.Payment;
import entities.Subscription;

import utils.DB_Context;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionService implements  CRUD<Subscription>{
    private final Connection conn = DB_Context.getInstance().getConn();

    @Override
    public void ajouter(Subscription sub) throws SQLException {
        String sql = "INSERT INTO subscription (`lines`, start_date, end_date, price, station_start, station_end, is_Valid) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sub.getLines());
            ps.setDate(2, Date.valueOf(sub.getStartDate()));
            ps.setDate(3, Date.valueOf(sub.getEndDate()));
            ps.setDouble(4, sub.getPrice());
            ps.setString(5, sub.getStationStart());
            ps.setString(6, sub.getStationEnd());
            ps.setBoolean(7, sub.isValid());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating subscription failed, no rows affected.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    sub.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Creating subscription failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public void supprimer(Subscription subscription) throws SQLException {
        String sql = "DELETE FROM subscription WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscription.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Deleting subscription failed, no rows affected.");
            }
        }
    }

    @Override
    public Subscription afficher(int id) throws SQLException {
        String sql = "SELECT * FROM subscription WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Subscription> afficher_tout() throws SQLException {
        String sql = "SELECT * FROM subscription";
        List<Subscription> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int getTotalSubscriptions() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM subscription";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    @Override
    public void modifier(Subscription sub) throws SQLException {
        String sql = "UPDATE subscription SET lines=?, start_date=?, end_date=?, price=?, station_start=?, station_end=?, is_Valid=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sub.getLines());
            ps.setDate(2, Date.valueOf(sub.getStartDate()));
            ps.setDate(3, Date.valueOf(sub.getEndDate()));
            ps.setDouble(4, sub.getPrice());
            ps.setString(5, sub.getStationStart());
            ps.setString(6, sub.getStationEnd());
            ps.setBoolean(7, sub.isValid());
            ps.setInt(8, sub.getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Updating subscription failed, no rows affected.");
            }
        }
    }


    private Subscription mapRow(ResultSet rs) throws SQLException {
        Subscription s = new Subscription();
        s.setId(rs.getInt("id"));
        s.setLines(rs.getString("lines"));
        s.setStartDate(rs.getDate("start_date").toLocalDate());
        s.setEndDate(rs.getDate("end_date").toLocalDate());
        s.setPrice(rs.getDouble("price"));
        s.setStationStart(rs.getString("station_start"));
        s.setStationEnd(rs.getString("station_end"));
        return s;
    }

    public void ajouterWithPayment(Subscription sub, Payment pay) throws SQLException {
        // both services share same Connection
        boolean oldAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1) insert subscription
            String sql = "INSERT INTO subscription (`lines`, start_date, end_date, price, station_start, station_end, is_Valid) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sub.getLines());
                ps.setDate(2, Date.valueOf(sub.getStartDate()));
                ps.setDate(3, Date.valueOf(sub.getEndDate()));
                ps.setDouble(4, sub.getPrice());
                ps.setString(5, sub.getStationStart());
                ps.setString(6, sub.getStationEnd());
                ps.setBoolean(7, sub.isValid());
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating subscription failed, no rows affected.");
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        sub.setId(keys.getInt(1));
                    } else {
                        throw new SQLException("Creating subscription failed, no ID obtained.");
                    }
                }
            }

            // 2) insert payment
            new PaymentService().ajouter(pay, sub.getId());

            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(oldAuto);
        }
    }

    public boolean isLineActive(String lineNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subscription WHERE FIND_IN_SET(?, `lines`) > 0 AND end_date >= CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lineNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Return true if any active subscription exists
                }
            }
        }
        return false;
    }


}
