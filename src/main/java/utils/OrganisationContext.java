package utils;

import entities.Organisation;

/**
 * Classe utilitaire pour stocker et accéder à l'organisation actuelle dans l'application.
 * Cette classe utilise le pattern Singleton pour garantir un accès global à l'objet Organisation.
 */
public class OrganisationContext {
    
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
        this.currentOrganisation = organisation;
    }
    
    /**
     * Récupère l'organisation actuelle
     * 
     * @return L'organisation courante
     */
    public Organisation getCurrentOrganisation() {
        return currentOrganisation;
    }
    
    /**
     * Récupère l'ID de l'organisation actuelle
     * 
     * @return L'ID de l'organisation courante ou 0 si aucune organisation n'est définie
     */
    public int getCurrentOrganisationId() {
        return currentOrganisation != null ? currentOrganisation.getId() : 0;
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
        this.currentOrganisation = null;
    }
}