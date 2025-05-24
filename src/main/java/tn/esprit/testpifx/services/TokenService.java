package tn.esprit.testpifx.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.models.VerificationToken;
import tn.esprit.testpifx.repositories.TokenRepository;
import tn.esprit.testpifx.utils.BrevoEmailService;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing verification tokens (account verification and password reset).
 */
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    
    private final TokenRepository tokenRepository;
    private final UserService userService;
    
    // Executor for scheduled tasks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * Constructs a TokenService with the specified repositories.
     * 
     * @param tokenRepository The repository for token operations
     * @param userService The user service for user operations
     */
    public TokenService(TokenRepository tokenRepository, UserService userService) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        
        // Schedule a task to clean up expired tokens daily
        scheduler.scheduleAtFixedRate(
            () -> {
                try {
                    tokenRepository.deleteExpiredTokens();
                } catch (Exception e) {
                    logger.error("Error cleaning up expired tokens: {}", e.getMessage(), e);
                }
            },
            1, // initial delay
            24, // period
            TimeUnit.HOURS // time unit
        );
    }
    
    /**
     * Creates a new account verification token and sends a verification email.
     * 
     * @param user The user to create a verification token for
     * @return The created verification token
     */
    public VerificationToken createAccountVerificationToken(User user) {
        // Validate user has an ID
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty when creating a verification token");
        }
        
        // Check if an unused token already exists
        Optional<VerificationToken> existingToken = tokenRepository.findActiveByUserAndType(
            user.getUserId(), 
            VerificationToken.TokenType.ACCOUNT_VERIFICATION
        );
        
        if (existingToken.isPresent() && !existingToken.get().isExpired()) {
            logger.info("Reusing existing active verification token for user: {}", user.getUsername());            
            BrevoEmailService.sendAccountVerificationEmail(
                user.getEmail(), 
                user.getUsername(), 
                existingToken.get().getToken()
            );
            return existingToken.get();
        }
        
        // Create new token
        VerificationToken token = new VerificationToken(
            user.getUserId(), 
            VerificationToken.TokenType.ACCOUNT_VERIFICATION
        );
    
        tokenRepository.save(token);
        logger.info("Created account verification token for user: {}", user.getUsername());
        // Send verification email
        BrevoEmailService.sendAccountVerificationEmail(
            user.getEmail(), 
            user.getUsername(), 
            token.getToken()
        );
        
        return token;
    }
    
    /**
     * Creates a new password reset token and sends a password reset email.
     * 
     * @param user The user to create a password reset token for
     * @return The created verification token
     */
    public VerificationToken createPasswordResetToken(User user) {
        // Validate user has an ID
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty when creating a password reset token");
        }
        
        // Check if an unused token already exists
        Optional<VerificationToken> existingToken = tokenRepository.findActiveByUserAndType(
            user.getUserId(), 
            VerificationToken.TokenType.PASSWORD_RESET
        );
        
        if (existingToken.isPresent() && !existingToken.get().isExpired()) {            
            logger.info("Reusing existing active password reset token for user: {}", user.getUsername());
            BrevoEmailService.sendPasswordResetEmail(
                user.getEmail(), 
                user.getUsername(), 
                existingToken.get().getToken()
            );
            return existingToken.get();
        }
        
        // Create new token
        VerificationToken token = new VerificationToken(
            user.getUserId(), 
            VerificationToken.TokenType.PASSWORD_RESET
        );
        
        tokenRepository.save(token);
        logger.info("Created password reset token for user: {}", user.getUsername());
        // Send password reset email
        BrevoEmailService.sendPasswordResetEmail(
            user.getEmail(), 
            user.getUsername(), 
            token.getToken()
        );
        
        return token;
    }
    
    /**
     * Verifies a token and marks it as used.
     * 
     * @param tokenStr The token string to verify
     * @return Optional containing the user if verification successful, empty otherwise
     */
    public Optional<User> verifyToken(String tokenStr) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        
        if (tokenOpt.isEmpty()) {
            logger.warn("Token not found: {}", tokenStr);
            return Optional.empty();
        }
        
        VerificationToken token = tokenOpt.get();
        
        if (!token.isValid()) {
            logger.warn("Token is invalid (expired or used): {}", tokenStr);
            return Optional.empty();
        }
        
        Optional<User> userOpt = userService.getUserById(token.getUserId());
        if (userOpt.isEmpty()) {
            logger.warn("User not found for token: {}", tokenStr);
            return Optional.empty();
        }
        
        // Token is valid, mark as used
        tokenRepository.markAsUsed(tokenStr);
        
        return userOpt;
    }
    
    /**
     * Validates an account verification token and activates the user's account.
     * 
     * @param tokenStr The verification token string
     * @return true if verification successful, false otherwise
     */
    public boolean verifyAccountToken(String tokenStr) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        VerificationToken token = tokenOpt.get();
        
        if (!token.isValid() || token.getTokenType() != VerificationToken.TokenType.ACCOUNT_VERIFICATION) {
            return false;
        }
        
        // Verify the token, mark as used, and activate the user
        Optional<User> userOpt = userService.getUserById(token.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setActive(true); // Activate user account
        userService.updateUser(user);
        
        // Mark token as used
        tokenRepository.markAsUsed(tokenStr);
        
        return true;
    }
    
    /**
     * Resets a user's password using a password reset token.
     * 
     * @param tokenStr The password reset token string
     * @param newPassword The new password
     * @return true if password reset successful, false otherwise
     */
    public boolean resetPassword(String tokenStr, String newPassword) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        VerificationToken token = tokenOpt.get();
        
        if (!token.isValid() || token.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
            return false;
        }
        
        // Reset password and mark token as used
        Optional<User> userOpt = userService.getUserById(token.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setPassword(newPassword);
        userService.updateUser(user);
        
        // Mark token as used
        tokenRepository.markAsUsed(tokenStr);
        
        return true;
    }
    
    /**
     * Gets the most recent active token for a user by type.
     * 
     * @param userId The user ID
     * @param tokenType The token type
     * @return Optional containing the token if found
     */
    public Optional<VerificationToken> getActiveTokenForUser(String userId, VerificationToken.TokenType tokenType) {
        return tokenRepository.findActiveByUserAndType(userId, tokenType);
    }
    
    /**
     * Shutdown the service and release resources.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}
