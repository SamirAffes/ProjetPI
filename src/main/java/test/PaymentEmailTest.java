package test;

import entities.Reservation;
import entities.User;
import services.EmailService;
import services.ReservationService;
import services.ReservationServiceImpl;
import services.UserService;

import java.time.LocalDateTime;

public class PaymentEmailTest {
    public static void main(String[] args) {
        System.out.println("Starting payment confirmation email test...");
        
        try {
            // Create test instances
            EmailService emailService = new EmailService();
            UserService userService = new UserService();
            
            // Test direct email sending
            testDirectEmailSending(emailService);
            
            // Test through reservation service
            testReservationEmailSending();
            
            System.out.println("Email tests completed!");
        } catch (Exception e) {
            System.err.println("Error in email test: " + e.getMessage());
            e.printStackTrace();
        }
    }
      private static void testDirectEmailSending(EmailService emailService) {
        System.out.println("Testing direct email sending...");
        
        try {
            // Replace with a valid email address for testing
            String testEmail = "votre.email@test.com";
            
            // Test HTML email with QR code
            boolean sent = emailService.sendPaymentConfirmationEmail(
                testEmail,
                "Test de Confirmation de Paiement - TunTransport",
                "Client Test",
                "TES123456",
                150.50,
                null
            );
            
            if (sent) {
                System.out.println("Email direct envoyé avec succès à " + testEmail);
            } else {
                System.err.println("Échec de l'envoi d'email direct à " + testEmail);
            }
            
            System.out.println("Test d'email direct terminé!");
        } catch (Exception e) {
            System.err.println("Erreur dans le test d'email direct: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testReservationEmailSending() {
        System.out.println("Testing reservation service email sending...");
        
        try {
            ReservationService reservationService = new ReservationServiceImpl();
            
            // Create a test reservation (use a real user ID that exists in your database)
            Reservation testReservation = new Reservation();
            testReservation.setUserId(1); // Use an existing user ID
            testReservation.setRouteId(1); // Use an existing route ID
            testReservation.setTransportId(1); // Use an existing transport ID
            testReservation.setDateTime(LocalDateTime.now().plusDays(3));
            testReservation.setPrice(175.00);
            testReservation.setIsPaid(true);
            testReservation.setPaid(true);
            testReservation.setStatus("CONFIRMED");
            
            // Add the reservation which should trigger the email sending
            reservationService.ajouter(testReservation);
            
            System.out.println("Reservation email test completed! Check the email inbox.");
        } catch (Exception e) {
            System.err.println("Error in reservation email test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
