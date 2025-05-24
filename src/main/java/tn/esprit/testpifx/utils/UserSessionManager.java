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
    
    // Add a static initialization block for debugging
    static {
        System.out.println("UserSessionManager initialized");
    }
    
    /**
     * Adds a user to the list of connected users.
     * 
     * @param user The user to add
     */    public static void addConnectedUser(User user) {
        connectedUsers.add(user);
        currentUser = user;
        System.out.println("User added to session: " + user.getUsername() + ", Connected users: " + connectedUsers.size());
    }
    
    /**
     * Removes a user from the list of connected users.
     * 
     * @param user The user to remove
     */
    public static void removeConnectedUser(User user) {
        boolean removed = connectedUsers.remove(user);
        System.out.println("Removing user from session: " + (user != null ? user.getUsername() : "null") + 
                          ", Success: " + removed + 
                          ", Remaining connected users: " + connectedUsers.size());
                          
        if (currentUser != null && currentUser.equals(user)) {
            System.out.println("Current user reset to null (was: " + currentUser.getUsername() + ")");
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
    }    /**
     * Returns the current user (first user in the connected users list).
     * 
     * @return The current user, or null if no user is connected
     */
    public static User getCurrentUser() {
        System.out.println("Getting current user: " + (currentUser != null ? currentUser.getUsername() : "null"));
        return currentUser;
    }
    
    /**
     * Checks if the session is valid (has a current user)
     * 
     * @return true if there is a valid user in the current session
     */
    public static boolean isSessionValid() {
        boolean valid = currentUser != null;
        System.out.println("Session validity check: " + valid);
        return valid;
    }    /**
     * Clears the list of connected users.
     * Useful for testing or when shutting down the application.
     */
    public static void clearSession() {
        System.out.println("Clearing session data. Connected users: " + connectedUsers.size() + 
                           ", current user: " + (currentUser != null ? currentUser.getUsername() : "null"));
        
        // Add a stack trace to see where the clearSession is being called from
        Exception e = new Exception("Session clear stack trace");
        e.printStackTrace();
        
        connectedUsers.clear();
        currentUser = null;
    }
}