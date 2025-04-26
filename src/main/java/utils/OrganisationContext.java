package utils;

import entities.Organisation;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe singleton qui permet de stocker et d'accéder à l'organisation courante
 * à travers toute l'application.
 */
@Slf4j
public class OrganisationContext {
    
    private static OrganisationContext instance;
    private Organisation currentOrganisation;
    
    private OrganisationContext() {
        // Constructeur privé pour empêcher l'instanciation directe
    }
    
    /**
     * Obtenir l'instance unique d'OrganisationContext
     * @return L'instance OrganisationContext
     */
    public static synchronized OrganisationContext getInstance() {
        if (instance == null) {
            instance = new OrganisationContext();
        }
        return instance;
    }
    
    /**
     * Définir l'organisation courante
     * @param organisation L'organisation à stocker
     */
    public void setCurrentOrganisation(Organisation organisation) {
        this.currentOrganisation = organisation;
        log.info("Organisation courante définie: {}", 
                organisation != null ? organisation.getNom() : "null");
    }
    
    /**
     * Récupérer l'organisation courante
     * @return L'organisation courante ou null si aucune n'est définie
     */
    public Organisation getCurrentOrganisation() {
        return currentOrganisation;
    }
    
    /**
     * Vérifier si une organisation est actuellement définie
     * @return true si une organisation est définie, sinon false
     */
    public boolean hasCurrentOrganisation() {
        return currentOrganisation != null;
    }
    
    /**
     * Effacer l'organisation courante
     */
    public void clearCurrentOrganisation() {
        if (currentOrganisation != null) {
            log.info("Organisation courante effacée: {}", currentOrganisation.getNom());
        }
        this.currentOrganisation = null;
    }
}