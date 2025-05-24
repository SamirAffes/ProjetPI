package tn.esprit.testpifx.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.InternetAddress;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for handling emails in the application using Brevo API.
 * This implementation sends real emails through Brevo (formerly SendinBlue).
 */
public class BrevoEmailService {    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailService.class);    // Brevo API configuration with your provided API key
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email"; // Try the new Brevo domain
    
    // Sender information with your validated sender email
    private static final String SENDER_EMAIL = "sifaoui.mustpha@gmail.com"; // Your Brevo verified sender
    private static final String SENDER_NAME = "Mustapha Sifaoui"; // Your name
    
    // Thread pool for asynchronous email operations
    private static final ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
    
    // HTTP Client for API requests
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    
    static {
        logger.info("Brevo email service initialized");
    }
    
    /**
     * Sends an email asynchronously using Brevo API.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body content (HTML supported)
     */
    public static void sendEmailAsync(String to, String subject, String body) {
        emailExecutor.submit(() -> {
            try {
                sendEmail(to, subject, body);
                logger.info("Email sent successfully to: {}", to);
            } catch (Exception e) {
                logger.error("Failed to send email to {}: {}", to, e.getMessage());
            }
        });
    }
    
    /**
     * Sends an email synchronously using Brevo API.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body content (HTML supported)
     * @throws Exception If there's an error sending the email
     */    public static void sendEmail(String to, String subject, String body) throws Exception {
        // Create JSON payload for Brevo API
        String jsonPayload = createEmailJson(to, subject, body);
        
        // Log request details for debugging
        logger.info("Sending email request to URL: {}", API_URL);
        logger.info("Using API Key: {}...", API_KEY.substring(0, 15) + "...");
        logger.info("Request payload: {}", jsonPayload);
          // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        
        // Log the exact request headers for debugging
        logger.info("Using API URL: {}", API_URL);
        logger.info("Request headers: Content-Type: application/json, Accept: application/json");
        logger.info("API Key header format: 'api-key: {}'", API_KEY.substring(0, 15) + "...");
        
        // Send the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Check response
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.info("Email sent successfully to {}, response: {}", to, response.body());
        } else {
            logger.error("Failed to send email. Status: {}, Response: {}", 
                    response.statusCode(), response.body());
            // Print all response headers for debugging
            response.headers().map().forEach((k, v) -> logger.info("Response Header: {} -> {}", k, v));
            throw new RuntimeException("Failed to send email: " + response.body());
        }
    }
      /**
     * Creates the JSON payload for Brevo API.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent Email HTML content
     * @return JSON string
     */
    private static String createEmailJson(String to, String subject, String htmlContent) {
        // Escaping HTML content for JSON inclusion
        String escapedHtml = htmlContent
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
            
        // Simple JSON construction
        return String.format(
            "{" +
            "\"sender\":{\"name\":\"%s\",\"email\":\"%s\"}," +
            "\"to\":[{\"email\":\"%s\"}]," +
            "\"subject\":\"%s\"," +
            "\"htmlContent\":\"%s\"" +
            "}", 
            SENDER_NAME.replace("\"", "\\\""), 
            SENDER_EMAIL, 
            to,
            subject.replace("\"", "\\\""), 
            escapedHtml
        );
    }
      /**
     * Sends a password reset email with a verification token.
     * 
     * @param to User's email address
     * @param username User's username
     * @param token Password reset token
     */
    public static void sendPasswordResetEmail(String to, String username, String token) {
        String subject = "Password Reset Request";
        // Use first 6 characters of token as a verification code
        String verificationCode = token.substring(0, 6).toUpperCase();
        
        String body = "<html><body>"
                + "<h2>Password Reset</h2>"
                + "<p>Hello " + username + ",</p>"
                + "<p>We received a request to reset your password. Your verification code is:</p>"
                + "<p style='font-size: 24px; font-weight: bold; text-align: center; padding: 10px; background-color: #f0f0f0; border-radius: 5px;'>" + verificationCode + "</p>"
                + "<p>Please enter this code in the application to reset your password.</p>"
                + "<p>If you didn't request this, please ignore this email.</p>"
                + "<p>This code will expire in 24 hours.</p>"
                + "<p>Best regards,<br/>The Team</p>"
                + "</body></html>";
                
        sendEmailAsync(to, subject, body);
    }
      /**
     * Sends an account verification email with a verification token.
     * 
     * @param to User's email address
     * @param username User's username
     * @param token Account verification token
     */
    public static void sendAccountVerificationEmail(String to, String username, String token) {
        String subject = "Verify Your Account";
        // Use first 6 characters of token as a verification code
        String verificationCode = token.substring(0, 6).toUpperCase();
        
        String body = "<html><body>"
                + "<h2>Account Verification</h2>"
                + "<p>Hello " + username + ",</p>"
                + "<p>Thank you for registering! Your verification code is:</p>"
                + "<p style='font-size: 24px; font-weight: bold; text-align: center; padding: 10px; background-color: #f0f0f0; border-radius: 5px;'>" + verificationCode + "</p>"
                + "<p>Please enter this code in the application to verify your account.</p>"
                + "<p>If you didn't create an account, please ignore this email.</p>"
                + "<p>This code will expire in 24 hours.</p>"
                + "<p>Best regards,<br/>The Team</p>"
                + "</body></html>";
                
        sendEmailAsync(to, subject, body);
    }
    
    /**
     * Sends a test email to verify Brevo API is working.
     * 
     * @param to Recipient email address
     * @return true if the email was sent successfully
     */
    public static boolean testEmail(String to) {
        try {
            String subject = "Test Email from Application";
            String body = "<html><body>"
                    + "<h2>Test Email</h2>"
                    + "<p>This is a test email to verify that the Brevo email service is working correctly.</p>"
                    + "<p>If you're seeing this, the email configuration is working!</p>"
                    + "</body></html>";
                    
            sendEmail(to, subject, body);
            return true;
        } catch (Exception e) {
            logger.error("Test email failed: {}", e.getMessage());
            return false;
        }
    }
}
