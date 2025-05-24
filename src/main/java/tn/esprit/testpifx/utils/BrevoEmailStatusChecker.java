package tn.esprit.testpifx.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility class for checking email status and activity in Brevo.
 */
public class BrevoEmailStatusChecker {
    // The API key
    private static final String API_KEY = "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";
    
    public static void main(String[] args) {
        System.out.println("Checking Brevo email activity...");
        
        // Get events for the last hour
        checkEmailEvents();
        
        // Check account settings
        checkSenderSettings();
        
        System.out.println("Email status check complete.");
    }
    
    private static void checkEmailEvents() {
        try {
            System.out.println("\n---- Checking recent email events ----");
            String eventsUrl = "https://api.brevo.com/v3/smtp/statistics/events";
            
            HttpClient client = HttpClient.newBuilder().build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(eventsUrl))
                    .header("accept", "application/json")
                    .header("api-key", API_KEY)
                    .GET()
                    .build();
            
            System.out.println("Sending GET request to: " + eventsUrl);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            
        } catch (Exception e) {
            System.out.println("Error checking email events: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkSenderSettings() {
        try {
            System.out.println("\n---- Checking sender settings ----");
            String sendersUrl = "https://api.brevo.com/v3/senders";
            
            HttpClient client = HttpClient.newBuilder().build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sendersUrl))
                    .header("accept", "application/json")
                    .header("api-key", API_KEY)
                    .GET()
                    .build();
            
            System.out.println("Sending GET request to: " + sendersUrl);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            
        } catch (Exception e) {
            System.out.println("Error checking sender settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
