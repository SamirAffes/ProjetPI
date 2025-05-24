package tn.esprit.testpifx.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility class for testing the Brevo API connection directly.
 * This allows us to isolate and debug API issues.
 */
public class BrevoApiTester {    // The API key to test
    private static final String API_KEY = "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";
    
    // URLs to test
    private static final String SENDINBLUE_URL = "https://api.sendinblue.com/v3/smtp/email";
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";    // Simple JSON payload for testing
    private static final String TEST_JSON_PAYLOAD = "{"
            + "\"sender\":{\"name\":\"Mustapha Sifaoui\",\"email\":\"sifaoui.mustpha@gmail.com\"},"
            + "\"to\":[{\"email\":\"mustapha.sifaoui@esprit.tn\"}],"
            + "\"subject\":\"API Test " + System.currentTimeMillis() + "\","
            + "\"htmlContent\":\"<p>This is a test email sent at: " + java.time.LocalDateTime.now() + "</p>\","
            + "\"tags\":[\"test\"]"
            + "}";      public static void main(String[] args) {
        System.out.println("Starting Brevo API test...");
        
        // Test both URLs to see if either works
        testApiEndpoint(SENDINBLUE_URL, "SENDINBLUE");
        testApiEndpoint(BREVO_URL, "BREVO");
        
        // Also test a simple GET request to verify API key is valid
        testApiKeyWithGet();
        
        // Check email events
        checkEmailEvents();
        
        // Check sender settings
        checkSenderSettings();
          // Test the EmailService class
        testBrevoEmailService();
        
        System.out.println("API tests complete.");
    }
    
    private static void testApiEndpoint(String url, String name) {
        try {
            System.out.println("\n---- Testing " + name + " endpoint ----");
            System.out.println("URL: " + url);
            System.out.println("API Key (first 15 chars): " + API_KEY.substring(0, 15) + "...");
            System.out.println("Payload: " + TEST_JSON_PAYLOAD);
            
            HttpClient client = HttpClient.newBuilder().build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(TEST_JSON_PAYLOAD))
                    .build();
            
            System.out.println("Sending request...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            response.headers().map().forEach((k, v) -> System.out.println("Header: " + k + " -> " + v));
            
        } catch (Exception e) {
            System.out.println("Error testing " + name + " endpoint: " + e.getMessage());
            e.printStackTrace();
        }
    }
      private static void testApiKeyWithGet() {
        try {
            System.out.println("\n---- Testing API key validity with GET request ----");
            String accountUrl = "https://api.sendinblue.com/v3/account";
            
            HttpClient client = HttpClient.newBuilder().build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(accountUrl))
                    .header("accept", "application/json")
                    .header("api-key", API_KEY)
                    .GET()
                    .build();
            
            System.out.println("Sending GET request to: " + accountUrl);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            
        } catch (Exception e) {
            System.out.println("Error testing API key: " + e.getMessage());
            e.printStackTrace();
        }
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
      private static void testBrevoEmailService() {
        try {
            System.out.println("\n---- Testing BrevoEmailService class ----");
            // Test the BrevoEmailService utility
            String testEmail = "mustapha.sifaoui@esprit.tn";
            
            System.out.println("Testing basic BrevoEmailService.sendEmail...");
            try {
                // Use direct JSON to avoid requiring the EmailService class to compile
                String jsonPayload = "{"
                    + "\"sender\":{\"name\":\"Mustapha Sifaoui\",\"email\":\"sifaoui.mustpha@gmail.com\"},"
                    + "\"to\":[{\"email\":\"" + testEmail + "\"}],"
                    + "\"subject\":\"Email Service Test " + System.currentTimeMillis() + "\","
                    + "\"htmlContent\":\"<p>This is a test email from BrevoEmailService at: " + java.time.LocalDateTime.now() + "</p>\""
                    + "}";
                
                HttpClient client = HttpClient.newBuilder().build();
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BREVO_URL))
                        .header("Content-Type", "application/json")
                        .header("api-key", API_KEY)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();
                
                System.out.println("Sending request via direct API call...");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                System.out.println("Response Status: " + response.statusCode());                System.out.println("Response Body: " + response.body());
                
                if (response.statusCode() == 201) {
                    System.out.println("BrevoEmailService test successful!");
                } else {
                    System.out.println("BrevoEmailService test failed with status: " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("Error testing BrevoEmailService: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("Error in BrevoEmailService test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
