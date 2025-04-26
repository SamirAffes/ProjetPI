package controllers;

import entities.ENUMS.EtatReclamation;
import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import services.ReclamationService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class AdminController implements Initializable {
    
    @FXML
    private ComboBox<EtatReclamation> etatFilterComboBox;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private TableView<Reclamation> reclamationsTable;
    
    @FXML
    private TableColumn<Reclamation, Integer> idColumn;
    
    @FXML
    private TableColumn<Reclamation, String> typeColumn;
    
    @FXML
    private TableColumn<Reclamation, String> descriptionColumn;
    
    @FXML
    private TableColumn<Reclamation, Date> dateColumn;
    
    @FXML
    private TableColumn<Reclamation, EtatReclamation> etatColumn;
    
    @FXML
    private TableColumn<Reclamation, Integer> userIdColumn;
    
    @FXML
    private TableColumn<Reclamation, Integer> chauffeurIdColumn;
    
    @FXML
    private TableColumn<Reclamation, Integer> organismeIdColumn;
    
    @FXML
    private Button modifierButton;
    
    @FXML
    private Button supprimerButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button returnButton;
    
    // Service pour gérer les réclamations
    private final ReclamationService reclamationService = new ReclamationService();
    
    // Liste observable pour le TableView
    private final ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes de la TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        chauffeurIdColumn.setCellValueFactory(new PropertyValueFactory<>("chauffeur_id"));
        organismeIdColumn.setCellValueFactory(new PropertyValueFactory<>("organisme_id"));
        
        // Configuration du format d'affichage de la date
        dateColumn.setCellFactory(column -> new TableCell<Reclamation, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });
        
        // Initialisation du ComboBox pour filtrer par état
        etatFilterComboBox.getItems().add(null); // Option pour afficher toutes les réclamations
        etatFilterComboBox.getItems().addAll(EtatReclamation.values());
        etatFilterComboBox.setConverter(new StringConverter<EtatReclamation>() {
            @Override
            public String toString(EtatReclamation etat) {
                if (etat == null) return "Tous les états";
                return etat.getEtat();
            }
            
            @Override
            public EtatReclamation fromString(String string) {
                return null; // Pas nécessaire pour notre usage
            }
        });
        
        // Ajout d'un écouteur pour le changement d'état du filtre
        etatFilterComboBox.setOnAction(event -> filterReclamations());
        
        // Ajout d'un écouteur pour la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterReclamations());
        
        // Charger les réclamations
        loadReclamations();
        
        // Configuration de la sélection pour activer/désactiver les boutons
        reclamationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            modifierButton.setDisable(!hasSelection);
            supprimerButton.setDisable(!hasSelection);
        });
        
        // Désactiver les boutons par défaut
        modifierButton.setDisable(true);
        supprimerButton.setDisable(true);
    }
    
    // Méthode pour charger toutes les réclamations
    private void loadReclamations() {
        try {
            List<Reclamation> allReclamations = reclamationService.afficher_tout();
            reclamationsList.setAll(allReclamations);
            reclamationsTable.setItems(reclamationsList);
        } catch (Exception e) {
            log.error("Erreur lors du chargement des réclamations: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les réclamations", e.getMessage());
        }
    }
    
    // Méthode pour filtrer les réclamations selon les critères
    private void filterReclamations() {
        EtatReclamation selectedEtat = etatFilterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();
        
        try {
            List<Reclamation> filteredReclamations;
            
            if (selectedEtat != null) {
                filteredReclamations = reclamationService.filtrerParEtat(selectedEtat);
            } else {
                filteredReclamations = reclamationService.afficher_tout();
            }
            
            // Filtre supplémentaire selon le texte de recherche
            if (!searchText.isEmpty()) {
                filteredReclamations.removeIf(reclamation -> 
                    !reclamation.getType().toLowerCase().contains(searchText) && 
                    !reclamation.getDescription().toLowerCase().contains(searchText)
                );
            }
            
            reclamationsTable.setItems(FXCollections.observableArrayList(filteredReclamations));
        } catch (Exception e) {
            log.error("Erreur lors du filtrage des réclamations: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de filtrer les réclamations", e.getMessage());
        }
    }
    
    @FXML
    void refreshList(ActionEvent event) {
        loadReclamations();
        etatFilterComboBox.setValue(null);
        searchField.clear();
    }
    
    @FXML
    void returnToHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent homeView = loader.load();
            Scene scene = new Scene(homeView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Système de Gestion des Réclamations");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Erreur lors du chargement de la page d'accueil: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil", e.getMessage());
        }
    }
    
    @FXML
    void handleModifierButton(ActionEvent event) {
        Reclamation selectedReclamation = reclamationsTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReclamationForm.fxml"));
                Parent formView = loader.load();
                
                // Récupérer le contrôleur et passer la réclamation sélectionnée
                ReclamationFormController controller = loader.getController();
                controller.initData(selectedReclamation, true); // true pour mode édition
                
                Stage stage = new Stage();
                stage.setTitle("Modifier une réclamation");
                stage.setScene(new Scene(formView));
                stage.showAndWait();
                
                // Rafraîchir la liste après modification
                loadReclamations();
            } catch (IOException e) {
                log.error("Erreur lors du chargement du formulaire de modification: {}", e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la réclamation", e.getMessage());
            }
        }
    }
    
    @FXML
    void handleSupprimerButton(ActionEvent event) {
        Reclamation selectedReclamation = reclamationsTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Supprimer la réclamation");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    reclamationService.supprimer(selectedReclamation);
                    loadReclamations();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réclamation supprimée", 
                            "La réclamation a été supprimée avec succès.");
                } catch (Exception e) {
                    log.error("Erreur lors de la suppression de la réclamation: {}", e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la réclamation", e.getMessage());
                }
            }
        }
    }
    
    // Méthode utilitaire pour afficher des alertes
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}