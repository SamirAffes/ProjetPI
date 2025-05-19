package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * This class is a TextField with auto-completion functionality.
 */
public class AutoCompleteTextField extends TextField {
    private final SortedSet<String> entries;
    private final ContextMenu entriesPopup;
    private String lastSelectedValue;
    private int maxEntries = 10;

    public AutoCompleteTextField() {
        super();
        this.entries = new TreeSet<>();
        this.entriesPopup = new ContextMenu();
        this.lastSelectedValue = null;

        setListener();
    }

    private void setListener() {
        // Add a focus listener to show dropdown on focus
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && getText().isEmpty()) {
                showSuggestions(entries, getText());
            }
        });

        // Filter and show suggestions based on text changes
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (isFocused() && (newValue.length() > 0 || oldValue.length() > 0)) {
                showSuggestions(entries, newValue);
            } else {
                entriesPopup.hide();
            }
        });

        // Handle Enter key to select the current value
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && entriesPopup.isShowing()) {
                // If there are suggestions shown, select the first one
                if (entriesPopup.getItems().size() > 0) {
                    CustomMenuItem firstItem = (CustomMenuItem) entriesPopup.getItems().get(0);
                    TextFlow flow = (TextFlow) firstItem.getContent();
                    String suggestion = ((Text) flow.getChildren().get(0)).getText();
                    selectValue(suggestion);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                entriesPopup.hide();
            } else if (event.getCode() == KeyCode.DOWN) {
                if (!entriesPopup.isShowing()) {
                    showSuggestions(entries, getText());
                }
                entriesPopup.requestFocus();
                event.consume();
            }
        });
    }

    /**
     * Sets the available entries for autocompletion.
     * @param entries The list of entries
     */
    public void setEntries(List<String> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
    }

    /**
     * Set max entries to display in dropdown
     * @param maxEntries Maximum number of entries to show
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Get the last selected value from the dropdown
     * @return The last selected suggestion
     */
    public String getLastSelectedValue() {
        return lastSelectedValue;
    }

    /**
     * Shows the suggestions based on current text
     * @param allEntries All possible entries
     * @param text Current text to filter by
     */
    private void showSuggestions(SortedSet<String> allEntries, String text) {
        // If entries list is empty, hide popup and return
        if (allEntries.isEmpty()) {
            entriesPopup.hide();
            return;
        }

        // Filter entries based on text input
        List<String> filteredEntries;
        if (text == null || text.isEmpty()) {
            // Show all entries if text is empty, up to maxEntries
            filteredEntries = new ArrayList<>(allEntries).stream()
                    .limit(maxEntries)
                    .collect(Collectors.toList());
        } else {
            // Filter entries that contain the text (case insensitive)
            String lowerCaseText = text.toLowerCase();
            filteredEntries = allEntries.stream()
                    .filter(entry -> entry.toLowerCase().contains(lowerCaseText))
                    .limit(maxEntries)
                    .collect(Collectors.toList());
        }

        // If no matches, hide popup and return
        if (filteredEntries.isEmpty()) {
            entriesPopup.hide();
            return;
        }

        // Clear existing items and build new suggestion list
        entriesPopup.getItems().clear();
        
        for (String entry : filteredEntries) {
            // Create a TextFlow to highlight matching part
            TextFlow entryFlow = createTextFlow(entry, text);
            
            // Create menu item with the highlighted text
            CustomMenuItem item = new CustomMenuItem(entryFlow, true);
            
            // Set action for when item is clicked
            item.setOnAction(actionEvent -> selectValue(entry));
            
            entriesPopup.getItems().add(item);
        }

        // Show popup below the text field
        if (!entriesPopup.isShowing()) {
            entriesPopup.show(this, Side.BOTTOM, 0, 0);
        }
    }
    
    /**
     * Creates a TextFlow with the search string highlighted in the text.
     * 
     * @param text The full text
     * @param searchText The text to highlight
     * @return A TextFlow with the highlighted search text
     */
    private TextFlow createTextFlow(String text, String searchText) {
        TextFlow flow = new TextFlow();
        
        if (searchText == null || searchText.isEmpty()) {
            // No highlighting needed if search text is empty
            Text regularText = new Text(text);
            regularText.setFont(Font.font("System", FontWeight.NORMAL, 14));
            flow.getChildren().add(regularText);
            return flow;
        }
        
        // Case insensitive search
        String lowerCaseText = text.toLowerCase();
        String lowerCaseSearch = searchText.toLowerCase();
        
        int index = lowerCaseText.indexOf(lowerCaseSearch);
        if (index >= 0) {
            // Add text before the match
            if (index > 0) {
                Text beforeMatch = new Text(text.substring(0, index));
                beforeMatch.setFont(Font.font("System", FontWeight.NORMAL, 14));
                flow.getChildren().add(beforeMatch);
            }
            
            // Add the highlighted match
            Text match = new Text(text.substring(index, index + searchText.length()));
            match.setFill(Color.valueOf("#3498db")); // Highlight color
            match.setFont(Font.font("System", FontWeight.BOLD, 14)); // Bold font for highlight
            flow.getChildren().add(match);
            
            // Add text after the match
            if (index + searchText.length() < text.length()) {
                Text afterMatch = new Text(text.substring(index + searchText.length()));
                afterMatch.setFont(Font.font("System", FontWeight.NORMAL, 14));
                flow.getChildren().add(afterMatch);
            }
        } else {
            // No match found, just add the whole text
            Text regularText = new Text(text);
            regularText.setFont(Font.font("System", FontWeight.NORMAL, 14));
            flow.getChildren().add(regularText);
        }
        
        return flow;
    }

    /**
     * Selects a value from the suggestion list
     * @param value The value to select
     */
    private void selectValue(String value) {
        setText(value);
        lastSelectedValue = value;
        entriesPopup.hide();
        
        // Move caret to end of text and trigger onChange event
        positionCaret(getText().length());
        fireEvent(new ActionEvent());
    }
} 