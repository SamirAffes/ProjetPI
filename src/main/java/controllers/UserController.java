package controllers;

import entities.Reclamation;
import javafx.collections.FXCollections;
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
import lombok.extern.slf4j.Slf4j;
import services.ReclamationService;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class UserController implements Initializable {

    @FXML
    private Button createReclamationButton;
    
    @FXML
    private Button returnButton;
    
    @FXML
    private TableView<Reclamation> mesReclamationsTable;
    
    @FXML
    private TableColumn<Reclamation, Integer> idColumn;
    
    @FXML
    private TableColumn<Reclamation, String> typeColumn;
    
    @FXML
    private TableColumn<Reclamation, String> descriptionColumn;
    
    @FXML
    private TableColumn<Reclamation, Date> dateColumn;
    
    @FXML
    private TableColumn<Reclamation, String> etatColumn;
    
    @FXML
    private TableColumn<Reclamation, String> chauffeurColumn;
    
    @FXML
    private TableColumn<Reclamation, String> organismeColumn;
    
    private final ReclamationService reclamationService = new ReclamationService();
    
    // ID de l'utilisateur courant (à remplacer par un système d'authentification réel)
    private final int currentUserId = 1; // Pour simuler un utilisateur connecté

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes de la TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        chauffeurColumn.setCellValueFactory(new PropertyValueFactory<>("chauffeur_id")); // À améliorer pour afficher le nom
        organismeColumn.setCellValueFactory(new PropertyValueFactory<>("organisme_id")); // À améliorer pour afficher le nom
        
        // Configuration du format d'affichage de la date
        dateColumn.setCellFactory(column -> new TableCell<Reclamation, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });
        
        // Charger les réclamations de l'utilisateur courant
        loadUserReclamations();
    }
    
    private void loadUserReclamations() {
        try {
            // Dans une application réelle, nous avons une méthode pour obtenir les réclamations par ID utilisateur
            // Ici, nous filtrons après récupération de toutes les réclamations
            List<Reclamation> allReclamations = reclamationService.afficher_tout();
            List<Reclamation> userReclamations = allReclamations.stream()
                    .filter(reclamation -> reclamation.getUser_id() == currentUserId)
                    .collect(Collectors.toList());
            
            mesReclamationsTable.setItems(FXCollections.observableArrayList(userReclamations));
            log.info("{} réclamations chargées pour l'utilisateur {}", userReclamations.size(), currentUserId);
        } catch (Exception e) {
            log.error("Erreur lors du chargement des réclamations de l'utilisateur: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger vos réclamations", e.getMessage());
        }
    }
    
    @FXML
    void handleCreateReclamation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReclamationForm.fxml"));
            Parent formView = loader.load();
            
            // Récupérer le contrôleur et initialiser pour une nouvelle réclamation
            ReclamationFormController controller = loader.getController();
            controller.initData(null, false); // null pour nouvelle réclamation, false pour mode création
            controller.setUserId(currentUserId);
            
            // Configurer la fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Créer une réclamation");
            stage.setScene(new Scene(formView));
            stage.showAndWait();
            
            // Rafraîchir la liste après création
            loadUserReclamations();
        } catch (IOException e) {
            log.error("Erreur lors du chargement du formulaire de création: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer une réclamation", e.getMessage());
        }
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
    
    // Méthode pour afficher des alertes
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}