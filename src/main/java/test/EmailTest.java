package test;

import services.EmailService;

public class EmailTest {
    public static void main(String[] args) {
        System.out.println("Starting email test...");
        
        EmailService emailService = new EmailService();
        
        try {
            // Replace with a valid email address for testing
            String testEmail = "test@example.com";
            
            emailService.sendEmail(
                testEmail,
                "Test Email",
                "This is a test email to verify that the email service is working correctly."
            );
            
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}