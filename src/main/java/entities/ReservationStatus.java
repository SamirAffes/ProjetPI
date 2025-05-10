package entities;

public enum ReservationStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmée"),
    CANCELED("Annulée"),
    COMPLETED("Terminée"),
    REJECTED("Rejetée");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
