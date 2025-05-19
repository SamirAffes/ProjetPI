package services;

import entities.Reclamation;
import entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.time.LocalDateTime;
import java.util.List;

public class ReclamationService {
    private final EntityManager em;
    private final EmailService emailService;

    public ReclamationService() {
        this.em = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.emailService = new EmailService();
    }

    public void createReclamation(Reclamation reclamation) {
        try {
            em.getTransaction().begin();
            reclamation.setCreationDate(LocalDateTime.now());
            reclamation.setStatus(Reclamation.ReclamationStatus.PENDING);
            em.persist(reclamation);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Reclamation> getUserReclamations(User user) {
        TypedQuery<Reclamation> query = em.createQuery(
            "SELECT r FROM Reclamation r WHERE r.user = :user ORDER BY r.creationDate DESC", 
            Reclamation.class
        );
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<Reclamation> getAllReclamations() {
        return em.createQuery("SELECT r FROM Reclamation r ORDER BY r.creationDate DESC", Reclamation.class)
                 .getResultList();
    }

    public void updateReclamation(Reclamation reclamation) {
        // First, ensure the reclamation exists in the database
        Reclamation existingReclamation = em.find(Reclamation.class, reclamation.getId());
        if (existingReclamation == null) {
            throw new IllegalArgumentException("Reclamation with ID " + reclamation.getId() + " not found");
        }

        // Set the response date
        reclamation.setResponseDate(LocalDateTime.now());

        // Update the reclamation in the database
        try {
            em.getTransaction().begin();
            Reclamation updatedReclamation = em.merge(reclamation);
            em.flush(); // Force synchronization with the database
            em.getTransaction().commit();

            // Send email notification to user after successful database update
            sendUpdateEmail(updatedReclamation);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to update reclamation: " + e.getMessage(), e);
        }
    }

    private void sendUpdateEmail(Reclamation reclamation) {
        try {
            String subject = "Update on your complaint: " + reclamation.getTitle();
            String message = String.format("""
                Dear %s,

                Your complaint has been updated:
                Status: %s
                Response: %s

                Best regards,
                Support Team
                """, 
                reclamation.getUser().getFullName(),
                reclamation.getStatus(),
                reclamation.getResponse()
            );

            emailService.sendEmail(reclamation.getUser().getEmail(), subject, message);
        } catch (Exception e) {
            // Log the error but don't fail the update operation
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
