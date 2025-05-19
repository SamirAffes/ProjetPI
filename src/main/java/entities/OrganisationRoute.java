package entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrganisationRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private int organisationId;
    
    @Column(nullable = false)
    private int routeId;
    
    private boolean isActive;
    
    // Additional fields for organization-specific route information
    private String internalRouteCode;
    private String assignedVehicleType;
    private int frequencyPerDay;
    private String departureTime;
    private String arrivalTime;
    private String notes;
    
    // New fields for enhanced functionality
    // Specific stations for this organization's route
    private Integer departureStationId;
    private Integer arrivalStationId;
    
    // Working hours for this route
    private String weekdaySchedule; // e.g., "08:00-20:00"
    private String saturdaySchedule;
    private String sundaySchedule;
    private String holidaySchedule;
    
    // First and last service times
    private String firstDepartureTime;
    private String lastDepartureTime;
    
    // Operational days (bitfield: 1=Monday, 2=Tuesday, 4=Wednesday, etc.)
    private int operationalDays = 127; // Default: All days (127 = 1+2+4+8+16+32+64)
    
    // Additional amenities and services
    @Builder.Default
    private Boolean wifiAvailable = false;
    
    @Column(name = "`accessible`")
    @Builder.Default
    private Boolean accessible = false;  // Accessibility for persons with reduced mobility
    
    @Builder.Default
    private Boolean airConditioned = false;
    
    @Builder.Default
    private Boolean foodService = false;
    
    // Route specific information
    private Double customPrice; // Can override the base route price
    private Integer customDuration; // Can override the base route duration
    
    // Legacy fields maintained for backward compatibility
    @Column(nullable = false)
    private int routeDuration = 0;
    
    @Column(nullable = false)
    private double routePrice = 0.0;
    
    // Platform or gate information
    private String platformInfo;
    
    // Timestamps
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
    
    /**
     * Check if this route operates on a specific day of week
     * @param dayOfWeek Day of week (1=Monday, 7=Sunday)
     * @return true if the route operates on this day
     */
    public boolean operatesOnDay(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) return false;
        int bitValue = 1 << (dayOfWeek - 1);
        return (operationalDays & bitValue) != 0;
    }
    
    /**
     * Set operation for a specific day of week
     * @param dayOfWeek Day of week (1=Monday, 7=Sunday)
     * @param operates true to enable operation on this day, false to disable
     */
    public void setOperationOnDay(int dayOfWeek, boolean operates) {
        if (dayOfWeek < 1 || dayOfWeek > 7) return;
        int bitValue = 1 << (dayOfWeek - 1);
        if (operates) {
            operationalDays |= bitValue;
        } else {
            operationalDays &= ~bitValue;
        }
    }
    
    /**
     * Convenience method to get the route price
     * Uses custom price if set, otherwise returns 0 (controller should get from Route)
     */
    public double getRoutePrice() {
        return customPrice != null ? customPrice : 0;
    }
    
    /**
     * Convenience method to get the route duration
     * Uses custom duration if set, otherwise returns 0 (controller should get from Route)
     */
    public int getRouteDuration() {
        return customDuration != null ? customDuration : 0;
    }
} 