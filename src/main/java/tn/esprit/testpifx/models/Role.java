package tn.esprit.testpifx.models;
import java.util.HashSet;
import java.util.Set;

/**
 * Enum representing the roles available in the system.
 * Each role has a set of permissions that determine what actions users with that role can perform.
 */
public enum Role {
    ADMIN(new HashSet<>(Set.of(
            // User-related permissions
            Permission.USER_CREATE,
            Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,
            Permission.USER_DISABLE,

            // Team-related permissions
            Permission.TEAM_CREATE,
            Permission.TEAM_READ,
            Permission.TEAM_UPDATE,
            Permission.TEAM_DELETE,
            Permission.TEAM_MEMBER_ADD,
            Permission.TEAM_MEMBER_REMOVE
    ))),

    MANAGER(new HashSet<>(Set.of(
            // User-related permissions
            Permission.USER_READ,
            Permission.USER_UPDATE,

            // Team-related permissions
            Permission.TEAM_CREATE,
            Permission.TEAM_READ,
            Permission.TEAM_UPDATE,
            Permission.TEAM_MEMBER_ADD,
            Permission.TEAM_MEMBER_REMOVE
    ))),

    USER(new HashSet<>(Set.of(
            // Team-related permissions (read-only)
            Permission.TEAM_READ
    )));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
