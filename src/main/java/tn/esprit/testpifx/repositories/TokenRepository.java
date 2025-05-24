package tn.esprit.testpifx.repositories;

import tn.esprit.testpifx.models.VerificationToken;

import java.util.Optional;

/**
 * Repository interface for VerificationToken data access.
 * Defines methods for creating, reading, and managing verification tokens.
 */
public interface TokenRepository {
    /**
     * Saves a verification token to the repository.
     * 
     * @param token The token to save
     */
    void save(VerificationToken token);
    
    /**
     * Finds a token by its string value.
     * 
     * @param token The token string to search for
     * @return An Optional containing the token if found, or empty if not found
     */
    Optional<VerificationToken> findByToken(String token);
    
    /**
     * Finds the most recent active (unused and not expired) token for a user and token type.
     * 
     * @param userId The user ID to search for
     * @param type The token type (ACCOUNT_VERIFICATION or PASSWORD_RESET)
     * @return An Optional containing the token if found, or empty if not found
     */
    Optional<VerificationToken> findActiveByUserAndType(String userId, VerificationToken.TokenType type);
    
    /**
     * Marks a token as used.
     * 
     * @param token The token string to mark as used
     * @return true if successful, false otherwise
     */
    boolean markAsUsed(String token);
    
    /**
     * Deletes all expired tokens from the repository.
     * This is useful for periodic cleanup.
     */
    void deleteExpiredTokens();
}
