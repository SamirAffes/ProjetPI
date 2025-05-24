package tn.esprit.testpifx.utils;

import java.util.prefs.Preferences;
import java.util.Base64;

/**
 * Utility class for managing user preferences like remembered credentials
 */
public class UserPreferences {
    
    private static final String PREF_USERNAME = "remembered_username";
    private static final String PREF_PASSWORD = "remembered_password";
    private static final String REMEMBER_ME = "remember_me";
    
    private static final Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);
    
    /**
     * Saves the user credentials if remember me is enabled
     * 
     * @param username Username to save
     * @param password Password to save (will be encoded, not encrypted)
     * @param rememberMe Whether to remember the credentials
     */
    public static void saveLoginCredentials(String username, String password, boolean rememberMe) {
        System.out.println("Saving login credentials, rememberMe=" + rememberMe);
        if (rememberMe) {
            prefs.put(PREF_USERNAME, username);
            // Simple encoding - not secure encryption
            String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
            prefs.put(PREF_PASSWORD, encodedPassword);
            prefs.putBoolean(REMEMBER_ME, true);
            try {
				prefs.flush();  // Ensure preferences are saved immediately to disk
			} catch (Exception e) {
				System.err.println("Error saving credentials: " + e.getMessage());
			}
            
            // Verify the credentials were saved correctly
            boolean saved = prefs.getBoolean(REMEMBER_ME, false);
            String savedUsername = prefs.get(PREF_USERNAME, "");
            System.out.println("Credentials saved successfully: " + saved + 
                              ", username match: " + savedUsername.equals(username));
        } else {
            clearLoginCredentials();
        }
    }
    
    /**
     * Clears saved login information
     */
    public static void clearLoginCredentials() {
        System.out.println("Clearing saved login credentials");
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_PASSWORD);
        prefs.putBoolean(REMEMBER_ME, false);
        try {
            prefs.flush();  // Ensure preferences are cleared immediately
            System.out.println("Credentials cleared from preferences");
        } catch (Exception e) {
            System.err.println("Error clearing credentials: " + e.getMessage());
        }
    }
    
    /**
     * Checks if credentials are saved
     */
    public static boolean hasRememberedCredentials() {
        boolean remember = prefs.getBoolean(REMEMBER_ME, false);
        String username = prefs.get(PREF_USERNAME, "");
        String password = prefs.get(PREF_PASSWORD, "");
        
        boolean hasCredentials = remember && !username.isEmpty() && !password.isEmpty();
        System.out.println("Checking for remembered credentials: " + hasCredentials + 
                          " (remember flag: " + remember + 
                          ", has username: " + !username.isEmpty() + 
                          ", has password: " + !password.isEmpty() + ")");
        return hasCredentials;
    }
    
    /**
     * Gets the saved username
     */
    public static String getUsername() {
        return prefs.get(PREF_USERNAME, "");
    }
    
    /**
     * Gets the saved password
     */    public static String getPassword() {
        String encodedPassword = prefs.get(PREF_PASSWORD, "");
        if (encodedPassword.isEmpty()) {
            System.out.println("No stored password found");
            return "";
        }
        // Decode the password
        try {
            String decodedPassword = new String(Base64.getDecoder().decode(encodedPassword));
            System.out.println("Retrieved stored password successfully");
            return decodedPassword;
        } catch (IllegalArgumentException e) {
            System.out.println("Error decoding stored password: " + e.getMessage());
            return "";
        }
    }
}
