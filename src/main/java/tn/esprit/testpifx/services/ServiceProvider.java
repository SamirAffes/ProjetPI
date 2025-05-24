package tn.esprit.testpifx.services;

import tn.esprit.testpifx.repositories.MySQLUserRepository;
import tn.esprit.testpifx.repositories.TokenRepositoryFactory;
import tn.esprit.testpifx.repositories.UserRepository;

/**
 * Provides singleton instances of services throughout the application.
 */
public class ServiceProvider {
    private static UserService userService;
    private static TeamService teamService;
    private static TokenService tokenService;
    
    /**
     * Gets the user service instance, creating it if it doesn't exist.
     * 
     * @return The user service instance
     */
    public static synchronized UserService getUserService() {
        if (userService == null) {
            UserRepository userRepository = new MySQLUserRepository();
            userService = new UserService(userRepository);
        }
        return userService;
    }
    
    /**
     * Gets the team service instance, creating it if it doesn't exist.
     * 
     * @return The team service instance
     */    public static synchronized TeamService getTeamService() {
        if (teamService == null) {
            // For now, create a new TeamRepository directly in the TeamService constructor
            // In a more sophisticated setup, we would have repository providers as well
            teamService = new TeamService(
                new tn.esprit.testpifx.repositories.MySQLTeamRepository(),
                new tn.esprit.testpifx.repositories.MySQLUserRepository()
            );
        }
        return teamService;
    }
    
    /**
     * Gets the token service instance, creating it if it doesn't exist.
     * 
     * @return The token service instance
     */
    public static synchronized TokenService getTokenService() {
        if (tokenService == null) {
            tokenService = new TokenService(
                TokenRepositoryFactory.createRepository(TokenRepositoryFactory.RepositoryType.MYSQL),
                getUserService()
            );
        }
        return tokenService;
    }
    
    /**
     * Sets the user service instance manually.
     * This is useful for testing or when you need to inject a custom implementation.
     * 
     * @param service The user service instance to set
     */
    public static void setUserService(UserService service) {
        userService = service;
    }
    
    /**
     * Sets the team service instance manually.
     * This is useful for testing or when you need to inject a custom implementation.
     * 
     * @param service The team service instance to set
     */
    public static void setTeamService(TeamService service) {
        teamService = service;
    }
    
    /**
     * Sets the token service instance manually.
     * This is useful for testing or when you need to inject a custom implementation.
     * 
     * @param service The token service instance to set
     */
    public static void setTokenService(TokenService service) {
        tokenService = service;
    }
}
