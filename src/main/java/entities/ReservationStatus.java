package entities;

/**
 * Enum representing the possible states of a reservation
 */
public enum ReservationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");
    
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
    
    // Helper method to convert from string to enum
    public static ReservationStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return PENDING; // Default value if null or empty
        }
        
        // Try exact name match first (PENDING, CONFIRMED, etc.)
        try {
            return valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If that fails, try display name match (Pending, Confirmed, etc.)
            for (ReservationStatus status : ReservationStatus.values()) {
                if (status.displayName.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            
            // If still not found, return default
            return PENDING;
        }
    }
} 