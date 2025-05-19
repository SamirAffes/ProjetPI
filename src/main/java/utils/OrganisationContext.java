package utils;

import entities.Organisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitaire pour stocker et accéder à l'organisation actuelle dans l'application.
 * Cette classe utilise le pattern Singleton pour garantir un accès global à l'objet Organisation.
 */
public class OrganisationContext {
    private static final Logger log = LoggerFactory.getLogger(OrganisationContext.class);
    
    private static OrganisationContext instance;
    private Organisation currentOrganisation;
    
    private OrganisationContext() {
        // Constructeur privé pour empêcher l'instanciation directe
    }
    
    /**
     * Obtient l'instance unique de OrganisationContext
     * 
     * @return L'instance de OrganisationContext
     */
    public static synchronized OrganisationContext getInstance() {
        if (instance == null) {
            instance = new OrganisationContext();
        }
        return instance;
    }
    
    /**
     * Définit l'organisation actuelle
     * 
     * @param organisation L'organisation à définir comme courante
     */
    public void setCurrentOrganisation(Organisation organisation) {
        if (organisation != null) {
            log.info("Setting current organisation to: {}", organisation.getNom());
        } else {
            log.info("Setting current organisation to null");
        }
        this.currentOrganisation = organisation;
    }
    
    /**
     * Récupère l'organisation actuelle
     * 
     * @return L'organisation courante
     */
    public Organisation getCurrentOrganisation() {
        if (currentOrganisation == null) {
            log.warn("Attempting to get current organisation, but it is null");
        }
        return currentOrganisation;
    }
    
    /**
     * Récupère l'ID de l'organisation actuelle
     * 
     * @return L'ID de l'organisation courante ou 0 si aucune organisation n'est définie
     */
    public int getCurrentOrganisationId() {
        if (currentOrganisation != null) {
            return currentOrganisation.getId();
        } else {
            log.warn("Attempting to get current organisation ID, but organisation is null");
            return 0;
        }
    }
    
    /**
     * Vérifie si une organisation est actuellement définie
     * 
     * @return true si une organisation est définie, false sinon
     */
    public boolean hasCurrentOrganisation() {
        return currentOrganisation != null;
    }
    
    /**
     * Efface l'organisation actuelle (par exemple lors de la déconnexion)
     */
    public void clearCurrentOrganisation() {
        log.info("Clearing current organisation");
        this.currentOrganisation = null;
    }
}