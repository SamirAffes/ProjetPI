package tn.esprit.testpifx.models;

/**
 * Enum representing the permissions available in the system.
 * These permissions control what actions each role can perform.
 */
public enum Permission {
    // User-related permissions
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    USER_DISABLE,

    // Team-related permissions
    TEAM_CREATE,
    TEAM_READ,
    TEAM_UPDATE,
    TEAM_DELETE,
    TEAM_MEMBER_ADD,
    TEAM_MEMBER_REMOVE
}
