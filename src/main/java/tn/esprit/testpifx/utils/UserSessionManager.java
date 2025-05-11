package tn.esprit.testpifx.utils;

import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for managing user sessions in the application.
 * This class keeps track of connected users and provides methods to access them.
 */
public class UserSessionManager {
    
    private static final Set<User> connectedUsers = new HashSet<>();
    private static User currentUser;
    
    /**
     * Adds a user to the list of connected users.
     * 
     * @param user The user to add
     */
    public static void addConnectedUser(User user) {
        connectedUsers.add(user);
        currentUser = user;
    }
    
    /**
     * Removes a user from the list of connected users.
     * 
     * @param user The user to remove
     */
    public static void removeConnectedUser(User user) {
        connectedUsers.remove(user);
        if (currentUser != null && currentUser.equals(user)) {
            currentUser = null;
        }
    }
    
    /**
     * Checks if a user is already in the list of connected users.
     * 
     * @param user The user to check
     * @return true if the user is connected, false otherwise
     */
    public static boolean isUserConnected(User user) {
        return connectedUsers.contains(user);
    }
    
    /**
     * Returns the list of all connected users.
     * 
     * @return List of connected users
     */
    public static Set<User> getConnectedUsers() {
        return new HashSet<>(connectedUsers);
    }
    
    /**
     * Returns the list of connected users with a specific role.
     * 
     * @param role The role to filter by
     * @return List of connected users with the specified role
     */
    public static List<User> getConnectedUsersByRole(Role role) {
        if (role == null) return Collections.emptyList();
        
        return connectedUsers.stream()
                .filter(user -> user.getRoles().contains(role))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the count of connected users.
     * 
     * @return Number of connected users
     */
    public static int getConnectedUsersCount() {
        return connectedUsers.size();
    }
    
    /**
     * Returns the count of connected users with a specific role.
     * 
     * @param role The role to filter by
     * @return Number of connected users with the specified role
     */
    public static int getConnectedUsersCountByRole(Role role) {
        if (role == null) return 0;
        
        return (int) connectedUsers.stream()
                .filter(user -> user.getRoles().contains(role))
                .count();
    }
    
    /**
     * Returns the current user (first user in the connected users list).
     * 
     * @return The current user, or null if no user is connected
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Clears the list of connected users.
     * Useful for testing or when shutting down the application.
     */
    public static void clearSession() {
        connectedUsers.clear();
        currentUser = null;
    }
}