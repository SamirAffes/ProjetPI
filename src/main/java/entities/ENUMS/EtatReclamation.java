package entities.ENUMS;

public enum EtatReclamation {
    EN_ATTENTE("En attente"),
    TRAITE("Traité"),
    REFUSE("Refusé"),
    EN_COURS("En cours"),
    TERMINE("Terminé");

    private String etat;

    EtatReclamation(String etat) {
        this.etat = etat;
    }

    public String getEtat() {
        return etat;
    }

    @Override
    public String toString() {
        return etat;
    }
}
