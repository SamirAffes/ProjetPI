package services;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for interacting with the Ollama API to generate chatbot responses.
 * Uses the qwen3:0.6b model and removes <think></think> tags from responses.
 */
@Slf4j
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "qwen3:0.6b";
    private static final String CUSTOM_MODEL_NAME = "tunbot";

    private final HttpClient httpClient;
    private boolean useCustomModel = false;

    // Store conversation history
    private StringBuilder conversationContext = new StringBuilder();

    public ChatbotService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends a message to the Ollama API and returns the response.
     * 
     * @param message The user's message
     * @return CompletableFuture with the chatbot's response
     */
    public CompletableFuture<String> sendMessage(String message) {
        try {
            // Update conversation context
            if (conversationContext.length() > 0) {
                conversationContext.append("\nUser: ").append(message);
            } else {
                conversationContext.append("User: ").append(message);
            }

            // Create request body
            String requestBody = createRequestBody(message);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(this::processResponse)
                    .exceptionally(e -> {
                        logger.error("Error communicating with Ollama API", e);
                        return "Sorry, I'm having trouble connecting to my brain right now. Please try again later.";
                    });
        } catch (Exception e) {
            logger.error("Error preparing request to Ollama API", e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("Sorry, I encountered an error. Please try again.");
            return future;
        }
    }

    /**
     * Creates the JSON request body for the Ollama API.
     * 
     * @param message The user's message
     * @return JSON string for the request body
     */
    private String createRequestBody(String message) {
        String modelToUse = useCustomModel ? CUSTOM_MODEL_NAME : MODEL_NAME;
        String prompt = conversationContext.toString() + "\nAssistant: ";

        // Simple JSON creation without external libraries
        return String.format(
                "{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":false}",
                modelToUse,
                escapeJson(prompt)
        );
    }

    /**
     * Processes the response from the Ollama API.
     * Removes <think></think> tags and extracts the actual response.
     * 
     * @param responseBody The raw response from the API
     * @return The processed response
     */
    private String processResponse(String responseBody) {
        try {
            logger.info("Raw response from Ollama API: " + responseBody); // Add debug logging
            // Extract the response text from the JSON
            String response = extractResponseFromJson(responseBody);
            logger.debug("Cleaned response from Ollama API: " + response);
            // Remove all variations of think tags (<think>, <tthink>, etc.)
            // First, handle properly matched tags with any number of 't's
            response = response.replaceAll("<t*think>.*?</t*think>", "").trim();
            // Then handle variations with different numbers of 't's in opening and closing tags
            response = response.replaceAll("<t+hink>.*?</t*hink>", "").trim();
            // Also handle any remaining think tags that might not be properly closed
            response = response.replaceAll("<t*think>[^<]*", "").trim();

            // Update conversation context with the assistant's response
            conversationContext.append("\nAssistant: ").append(response);

            return response;
        } catch (Exception e) {
            logger.error("Error processing Ollama API response", e);
            return "Sorry, I couldn't understand the response. Please try again.";
        }
    }

    /**
     * Extracts the response text from the JSON response.
     * 
     * @param json The JSON response from the API
     * @return The extracted response text
     */
 private String extractResponseFromJson(String json) {
    try {
        // Find the "response" field
        int startIndex = json.indexOf("\"response\":\"");
        if (startIndex != -1) {
            startIndex += 12; // Length of "response":"\
            int endIndex = json.indexOf("\",\"done\":", startIndex);
            if (endIndex != -1) {
                String response = json.substring(startIndex, endIndex)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\u003c", "<")
                    .replace("\\u003e", ">");
                
                logger.debug("Raw response: " + response); // Add debug logging
                
                // Remove think tags and get meaningful content
                String cleanResponse = response;
                if (response.contains("<think>") || response.contains("\\u003cthink")) {
                    cleanResponse = response.replaceAll("(?s)<think>.*?</think>", "")
                                         .replaceAll("(?s)\\u003cthink\\u003e.*?\\u003c/think\\u003e", "")
                                         .trim();
                    
                    // If response is empty after removing think tags, try to get the last non-empty line
                    if (cleanResponse.isEmpty()) {
                        String[] lines = response.split("\n");
                        for (int i = lines.length - 1; i >= 0; i--) {
                            String line = lines[i].trim();
                            if (!line.isEmpty() && !line.contains("<think>") && !line.contains("\\u003cthink")) {
                                cleanResponse = line;
                                break;
                            }
                        }
                    }
                }
                
                logger.debug("Cleaned response: " + cleanResponse); // Add debug logging
                
                return cleanResponse.isEmpty() ? "I couldn't generate a proper response." : cleanResponse;
            }
        }
        return "I couldn't generate a proper response.";
    } catch (Exception e) {
        logger.error("Error extracting response from JSON", e);
        return "I couldn't generate a proper response.";
    }
}
    /**
     * Escapes special characters in a string for JSON.
     * 
     * @param input The input string
     * @return The escaped string
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Clears the conversation context.
     */
    public void clearConversation() {
        conversationContext = new StringBuilder();
    }

    /**
     * Toggles between the default model and the custom TunBot model.
     * 
     * @param useCustom Whether to use the custom model
     */
    public void setUseCustomModel(boolean useCustom) {
        this.useCustomModel = useCustom;
    }

    /**
     * Checks if the custom model is being used.
     * 
     * @return true if using the custom model, false otherwise
     */
    public boolean isUsingCustomModel() {
        return useCustomModel;
    }

    /**
     * Enhances the prompt with relevant route data for RAG functionality.
     * 
     * @param message The user's message
     * @param routeData The route data to include
     * @return The enhanced message
     */
    public CompletableFuture<String> sendMessageWithRAG(String message, String routeData) {
        try {
            // Create a context-enhanced prompt
            String enhancedPrompt = "Here is some relevant information about routes:\n" + 
                                   routeData + 
                                   "\n\nUser question: " + message;

            // Update conversation context
            if (conversationContext.length() > 0) {
                conversationContext.append("\nUser: ").append(enhancedPrompt);
            } else {
                conversationContext.append("User: ").append(enhancedPrompt);
            }

            // Create request body
            String requestBody = createRequestBody(enhancedPrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(this::processResponse)
                    .exceptionally(e -> {
                        logger.error("Error communicating with Ollama API for RAG", e);
                        return "Sorry, I'm having trouble processing your question with the route data. Please try again.";
                    });
        } catch (Exception e) {
            logger.error("Error preparing RAG request to Ollama API", e);
            CompletableFuture<String> future = new CompletableFuture<>();
            future.complete("Sorry, I encountered an error with the RAG functionality. Please try again.");
            return future;
        }
    }
}
