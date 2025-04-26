package controllers;

import entities.ENUMS.EtatReclamation;
import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import services.ReclamationService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class ReclamationFormController implements Initializable {
    
    @FXML
    private Label formTitleLabel;
    
    @FXML
    private TextField typeField;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private ComboBox<EtatReclamation> etatComboBox;
    
    @FXML
    private ComboBox<ChauffeurOption> chauffeurComboBox;
    
    @FXML
    private ComboBox<OrganismeOption> organismeComboBox;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private final ReclamationService reclamationService = new ReclamationService();
    private Reclamation currentReclamation;
    private boolean editMode = false;
    private int userId = 1; // Valeur par défaut, à remplacer dans une application réelle

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le ComboBox des états
        etatComboBox.setItems(FXCollections.observableArrayList(EtatReclamation.values()));
        etatComboBox.setValue(EtatReclamation.EN_ATTENTE);
        etatComboBox.setConverter(new StringConverter<EtatReclamation>() {
            @Override
            public String toString(EtatReclamation etat) {
                return etat == null ? null : etat.getEtat();
            }
            
            @Override
            public EtatReclamation fromString(String string) {
                return null; // Pas nécessaire car on n'édite pas le texte directement
            }
        });
        
        // Initialiser les données de chauffeurs mockées
        initMockChauffeurs();
        
        // Initialiser les données d'organismes mockées
        initMockOrganismes();
    }
    
    // Données mockées pour les chauffeurs
    private void initMockChauffeurs() {
        List<ChauffeurOption> chauffeurs = Arrays.asList(
            new ChauffeurOption(1, "Ahmed Ben Ali"),
            new ChauffeurOption(2, "Mohamed Trabelsi"),
            new ChauffeurOption(3, "Sami Khelifi"),
            new ChauffeurOption(4, "Karim Mansouri"),
            new ChauffeurOption(5, "Hichem Bouazizi")
        );
        chauffeurComboBox.setItems(FXCollections.observableArrayList(chauffeurs));
        chauffeurComboBox.setConverter(new StringConverter<ChauffeurOption>() {
            @Override
            public String toString(ChauffeurOption chauffeur) {
                return chauffeur == null ? null : chauffeur.getNom();
            }
            
            @Override
            public ChauffeurOption fromString(String string) {
                return null;
            }
        });
    }
    
    // Données mockées pour les organismes
    private void initMockOrganismes() {
        List<OrganismeOption> organismes = Arrays.asList(
            new OrganismeOption(1, "TunTransport"),
            new OrganismeOption(2, "TRANSTU"),
            new OrganismeOption(3, "STS"),
            new OrganismeOption(4, "TCV"),
            new OrganismeOption(5, "Transport Express")
        );
        organismeComboBox.setItems(FXCollections.observableArrayList(organismes));
        organismeComboBox.setConverter(new StringConverter<OrganismeOption>() {
            @Override
            public String toString(OrganismeOption organisme) {
                return organisme == null ? null : organisme.getNom();
            }
            
            @Override
            public OrganismeOption fromString(String string) {
                return null;
            }
        });
    }
    
    // Initialiser les données du formulaire
    public void initData(Reclamation reclamation, boolean isEditMode) {
        this.editMode = isEditMode;
        
        if (isEditMode && reclamation != null) {
            this.currentReclamation = reclamation;
            formTitleLabel.setText("Modifier une réclamation");
            
            // Remplir les champs avec les valeurs de la réclamation existante
            typeField.setText(reclamation.getType());
            descriptionArea.setText(reclamation.getDescription());
            
            // Convertir Date en LocalDate pour le DatePicker
            LocalDate date = reclamation.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            datePicker.setValue(date);
            
            // Sélectionner l'état
            etatComboBox.setValue(reclamation.getEtat());
            
            // Sélectionner le chauffeur correspondant
            for (ChauffeurOption chauffeur : chauffeurComboBox.getItems()) {
                if (chauffeur.getId() == reclamation.getChauffeur_id()) {
                    chauffeurComboBox.setValue(chauffeur);
                    break;
                }
            }
            
            // Sélectionner l'organisme correspondant
            for (OrganismeOption organisme : organismeComboBox.getItems()) {
                if (organisme.getId() == reclamation.getOrganisme_id()) {
                    organismeComboBox.setValue(organisme);
                    break;
                }
            }
        } else {
            // Nouvelle réclamation
            this.currentReclamation = new Reclamation();
            formTitleLabel.setText("Créer une réclamation");
            datePicker.setValue(LocalDate.now());
            etatComboBox.setValue(EtatReclamation.EN_ATTENTE);
        }
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @FXML
    void handleSave(ActionEvent event) {
        if (!validateForm()) {
            // Le formulaire n'est pas valide
            return;
        }
        
        try {
            // Récupérer les valeurs du formulaire
            String type = typeField.getText().trim();
            String description = descriptionArea.getText().trim();
            Date date = java.util.Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            EtatReclamation etat = etatComboBox.getValue();
            int chauffeurId = chauffeurComboBox.getValue().getId();
            int organismeId = organismeComboBox.getValue().getId();
            
            // Mettre à jour l'objet Reclamation
            if (currentReclamation == null) {
                currentReclamation = new Reclamation();
            }
            
            currentReclamation.setType(type);
            currentReclamation.setDescription(description);
            currentReclamation.setDate(date);
            currentReclamation.setEtat(etat);
            currentReclamation.setUser_id(userId);
            currentReclamation.setChauffeur_id(chauffeurId);
            currentReclamation.setOrganisme_id(organismeId);
            
            // Enregistrer dans la base de données
            if (editMode) {
                reclamationService.modifier(currentReclamation);
                log.info("Réclamation modifiée avec succès: {}", currentReclamation.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Modification réussie", 
                        "La réclamation a été modifiée avec succès.");
            } else {
                reclamationService.ajouter(currentReclamation);
                log.info("Nouvelle réclamation ajoutée");
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Création réussie", 
                        "La réclamation a été créée avec succès.");
            }
            
            // Fermer la fenêtre
            closeWindow();
            
        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de la réclamation: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement", 
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    // Valider les données du formulaire
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (typeField.getText() == null || typeField.getText().trim().isEmpty()) {
            errorMessage.append("Le type de réclamation est obligatoire.\n");
        }
        
        if (datePicker.getValue() == null) {
            errorMessage.append("La date est obligatoire.\n");
        }
        
        if (etatComboBox.getValue() == null) {
            errorMessage.append("L'état est obligatoire.\n");
        }
        
        if (chauffeurComboBox.getValue() == null) {
            errorMessage.append("Vous devez sélectionner un chauffeur.\n");
        }
        
        if (organismeComboBox.getValue() == null) {
            errorMessage.append("Vous devez sélectionner un organisme.\n");
        }
        
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            errorMessage.append("La description est obligatoire.\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                    "Veuillez corriger les erreurs suivantes:", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    // Utilitaire pour afficher des alertes
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Fermer la fenêtre
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    // Classes internes pour les options de sélection
    public static class ChauffeurOption {
        private final int id;
        private final String nom;
        
        public ChauffeurOption(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNom() {
            return nom;
        }
    }
    
    public static class OrganismeOption {
        private final int id;
        private final String nom;
        
        public OrganismeOption(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNom() {
            return nom;
        }
    }
}