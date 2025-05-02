package services;

import entities.Payment;
import utils.DB_Context;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentService implements CRUD<Payment> {

    private final Connection conn = DB_Context.getInstance().getConn();

    /**
     * Inserts a Payment using its internal subscriptionId field.
     * Delegates to the more specific ajouter(p, subscriptionId).
     */
    @Override
    public void ajouter(Payment payment) throws SQLException {
        if (payment.getSubscriptionId() <= 0) {
            throw new SQLException("Subscription ID is not set on Payment.");
        }
        ajouter(payment, payment.getSubscriptionId());
    }

    /**
     * Inserts a Payment for a given subscription in one call.
     */
    public void ajouter(Payment p, int subscriptionId) throws SQLException {
        String sql = "INSERT INTO payment (subscription_id, date, amount, email, full_name, phone, card_number, card_expiry, card_cvv) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, subscriptionId);
            ps.setDate(2, Date.valueOf(p.getDate()));
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getFullName());
            ps.setString(6, p.getPhone());
            ps.setString(7, p.getCardNumber());
            ps.setString(8, p.getCardExpiry());
            ps.setString(9, p.getCardCvv());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public void supprimer(Payment payment) throws SQLException {
        String sql = "DELETE FROM payment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Deleting payment failed, no rows affected.");
            }
        }
    }

    @Override
    public void modifier(Payment payment) throws SQLException {
        String sql = "UPDATE payment SET date = ?, amount = ?, email = ?, full_name = ?, phone = ?, card_number = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(payment.getDate()));
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getEmail());
            ps.setString(4, payment.getFullName());
            ps.setString(5, payment.getPhone());
            ps.setString(6, payment.getCardNumber());
            ps.setInt(7, payment.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Updating payment failed, no rows affected.");
            }
        }
    }

    @Override
    public Payment afficher(int id) throws SQLException {
        String sql = "SELECT * FROM payment WHERE id = ?";
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
    public List<Payment> afficher_tout() throws SQLException {
        String sql = "SELECT * FROM payment";
        List<Payment> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setSubscriptionId(rs.getInt("subscription_id"));
        p.setDate(rs.getDate("date").toLocalDate());
        p.setAmount(rs.getDouble("amount"));
        p.setEmail(rs.getString("email"));
        p.setFullName(rs.getString("full_name"));
        p.setPhone(rs.getString("phone"));
        p.setCardNumber(rs.getString("card_number"));
        p.setCardExpiry(rs.getString("card_expiry"));
        p.setCardCvv(rs.getString("card_cvv"));

        return p;
    }

    public List<Payment> getBySubscriptionId(int subscriptionId) throws SQLException {
        String sql = "SELECT * FROM payment WHERE subscription_id = ?";
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }
}
