package tn.esprit.testpifx.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a verification token for account verification or password reset.
 */
public class VerificationToken {
    private String token;
    private String userId;
    private TokenType tokenType;
    private LocalDateTime expiryDate;
    private boolean used;
    
    /**
     * Token types for different verification purposes.
     */
    public enum TokenType {
        ACCOUNT_VERIFICATION,
        PASSWORD_RESET
    }
    
    /**
     * Creates a new verification token with default expiration (24 hours).
     * 
     * @param userId The user ID this token is associated with
     * @param tokenType The type of token (account verification or password reset)
     */
    public VerificationToken(String userId, TokenType tokenType) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.tokenType = tokenType;
        this.expiryDate = LocalDateTime.now().plusHours(24);
        this.used = false;
    }
    
    /**
     * Creates a new verification token with custom expiration.
     * 
     * @param userId The user ID this token is associated with
     * @param tokenType The type of token (account verification or password reset)
     * @param expirationHours Number of hours until the token expires
     */
    public VerificationToken(String userId, TokenType tokenType, int expirationHours) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.tokenType = tokenType;
        this.expiryDate = LocalDateTime.now().plusHours(expirationHours);
        this.used = false;
    }
    
    /**
     * Checks if the token has expired.
     * 
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * Checks if the token is valid (not expired and not used).
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }
    
    /**
     * Marks the token as used.
     */
    public void markAsUsed() {
        this.used = true;
    }

    // Getters and setters
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
