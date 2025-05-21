package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ChatbotService;
import services.RouteService;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the chatbot UI component.
 */
public class ChatbotController {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @FXML
    private VBox chatbotContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button closeChatButton;

    @FXML
    private ToggleButton modelToggleButton;

    private ChatbotService chatbotService;
    private RouteService routeService;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        logger.info("Initializing ChatbotController");

        chatbotService = new ChatbotService();
        routeService = new RouteService();

        // Add welcome message
        addBotMessage("Hello! I'm TunBot, your transport assistant. How can I help you today?");

        // Set up model toggle button
        modelToggleButton.setSelected(false);
        modelToggleButton.setText("Using: qwen3:0.6b");

        // Make sure the scroll pane scrolls to the bottom when new messages are added
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    /**
     * Handles the send button click event.
     */
    @FXML
    public void onSendButtonClick() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            // Add user message to the chat
            addUserMessage(message);

            // Clear the input field
            messageInput.clear();

            // Show typing indicator
            showTypingIndicator();

            // Check if the message is related to routes
            if (isRouteRelatedQuery(message)) {
                // Get route data and use RAG
                String routeData = routeService.getRelevantRouteData();
                chatbotService.sendMessageWithRAG(message, routeData)
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            // Remove typing indicator
                            removeTypingIndicator();
                            // Add bot response
                            addBotMessage(response);
                        });
                    });
            } else {
                // Regular query without RAG
                chatbotService.sendMessage(message)
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            // Remove typing indicator
                            removeTypingIndicator();
                            // Add bot response
                            addBotMessage(response);
                        });
                    });
            }
        }
    }

    /**
     * Handles the close chat button click event.
     */
    @FXML
    public void onCloseChatButtonClick() {
        // Hide the chatbot container
        chatbotContainer.setVisible(false);
        chatbotContainer.setManaged(false);

        logger.info("Chatbot closed");
    }

    /**
     * Handles the model toggle button click event.
     */
    @FXML
    public void onModelToggleButtonClick() {
        boolean useCustomModel = modelToggleButton.isSelected();
        chatbotService.setUseCustomModel(useCustomModel);

        if (useCustomModel) {
            modelToggleButton.setText("Using: TunBot");
            addBotMessage("Switched to TunBot model. This model has been fine-tuned for transportation queries in Tunisia.");
        } else {
            modelToggleButton.setText("Using: qwen3:0.6b");
            addBotMessage("Switched to qwen3:0.6b model.");
        }

        logger.info("Model switched to: {}", useCustomModel ? "TunBot" : "qwen3:0.6b");
    }

    /**
     * Adds a user message to the chat.
     * 
     * @param message The message to add
     */
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle(
                "-fx-background-color: #4285F4;" +
                "-fx-background-radius: 20px;" +
                "-fx-padding: 10px;"
        );
        text.setFill(Color.WHITE);

        messageBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            messagesContainer.getChildren().add(messageBox);
            chatScrollPane.setVvalue(1.0);
        });

        logger.info("User message added: {}", message);
    }

    /**
     * Adds a bot message to the chat.
     * 
     * @param message The message to add
     */
    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 5));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle(
                "-fx-background-color: #E0E0E0;" +
                "-fx-background-radius: 20px;" +
                "-fx-padding: 10px;"
        );

        messageBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            messagesContainer.getChildren().add(messageBox);
            chatScrollPane.setVvalue(1.0);
        });

        logger.info("Bot message added: {}", message);
    }

    /**
     * Shows a typing indicator in the chat.
     */
    private void showTypingIndicator() {
        HBox indicatorBox = new HBox();
        indicatorBox.setId("typingIndicator");
        indicatorBox.setAlignment(Pos.CENTER_LEFT);
        indicatorBox.setPadding(new Insets(5, 10, 5, 5));

        Text text = new Text("TunBot is typing...");
        text.setFont(Font.font("System", 12));
        text.setStyle("-fx-font-style: italic;");
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle(
                "-fx-background-color: #F0F0F0;" +
                "-fx-background-radius: 20px;" +
                "-fx-padding: 8px;"
        );

        indicatorBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            messagesContainer.getChildren().add(indicatorBox);
            chatScrollPane.setVvalue(1.0);
        });
    }

    /**
     * Removes the typing indicator from the chat.
     */
    private void removeTypingIndicator() {
        Platform.runLater(() -> {
            messagesContainer.getChildren().removeIf(node -> 
                node instanceof HBox && "typingIndicator".equals(node.getId()));
        });
    }

    /**
     * Checks if a message is related to routes.
     * 
     * @param message The message to check
     * @return true if the message is related to routes, false otherwise
     */
    private boolean isRouteRelatedQuery(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("route") || 
               lowerMessage.contains("transport") || 
               lowerMessage.contains("bus") || 
               lowerMessage.contains("train") || 
               lowerMessage.contains("travel") || 
               lowerMessage.contains("trip") ||
               lowerMessage.contains("from") && lowerMessage.contains("to") ||
               lowerMessage.contains("schedule") ||
               lowerMessage.contains("timetable");
    }

    /**
     * Shows the chatbot UI.
     */
    public void show() {
        chatbotContainer.setVisible(true);
        chatbotContainer.setManaged(true);

        // Clear the input field
        messageInput.clear();

        logger.info("Chatbot shown");
    }

    /**
     * Hides the chatbot UI.
     */
    public void hide() {
        chatbotContainer.setVisible(false);
        chatbotContainer.setManaged(false);

        logger.info("Chatbot hidden");
    }

    /**
     * Toggles the visibility of the chatbot UI.
     * 
     * @return true if the chatbot is now visible, false otherwise
     */
    public boolean toggle() {
        boolean newVisibility = !chatbotContainer.isVisible();
        chatbotContainer.setVisible(newVisibility);
        chatbotContainer.setManaged(newVisibility);

        logger.info("Chatbot visibility toggled to: {}", newVisibility);

        return newVisibility;
    }
}
