package tn.esprit.testpifx.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.repositories.TokenRepository;
import tn.esprit.testpifx.repositories.TokenRepositoryFactory;
import tn.esprit.testpifx.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final boolean initializeAdmin;
    private TokenRepository tokenRepository;

    public UserService(UserRepository userRepository) {
        this(userRepository, true);
    }

    public UserService(UserRepository userRepository, boolean initializeAdmin) {
        this.userRepository = userRepository;
        this.initializeAdmin = initializeAdmin;
        // Initialize token repository for verification and password reset
        this.tokenRepository = TokenRepositoryFactory.createRepository(
            TokenRepositoryFactory.RepositoryType.MYSQL
        );
        
        if (initializeAdmin) {
            initializeAdminUser();
        }
    }

    public void initializeAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "admin", "admin@university.edu");
            admin.getRoles().add(Role.ADMIN);
            admin.setActive(true); // Admin is always active
            userRepository.save(admin);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public void disableUser(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
    }

    public void enableUser(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password) && user.isActive());
    }

    /**
     * Finds a user by their username.
     * 
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by their email address.
     * 
     * @param email The email address to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);    }    /**
     * Creates a new user.
     * The user is always set as active for simplicity.
     * 
     * @param user The user to create
     * @param requireVerification Parameter is ignored, kept for compatibility
     * @return The created user
     */    public User createUser(User user, boolean requireVerification) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
          // Set user ID if not set
        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID().toString());
        }
        
        // Always set user as active - no verification needed
        user.setActive(true);
        
        // Ensure the user has at least the USER role
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.getRoles().add(Role.USER);
        }
          try {            
            logger.info("Creating new user: {} with ID: {}", user.getUsername(), user.getUserId());
            
            // Save the user to the repository
            User savedUser = userRepository.save(user);
            logger.info("User saved successfully: {} with ID: {}", savedUser.getUsername(), savedUser.getUserId());
            
            // Verify the user was saved by retrieving it from the repository
            Optional<User> retrievedUser = userRepository.findById(savedUser.getUserId());
            if (retrievedUser.isPresent()) {
                logger.info("User retrieval verification successful - User {} found in database", savedUser.getUsername());
                return savedUser;
            } else {
                logger.error("Failed to verify user creation, user ID {} not found after save operation", savedUser.getUserId());
                throw new RuntimeException("Failed to verify user creation, user ID not found after save operation");
            }
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a new user that requires email verification.
     * 
     * @param user The user to create
     * @return The created user
     */
    public User createUser(User user) {
        return createUser(user, true);
    }    /**
     * Sends an account verification email to the user.
     * 
     * @param user The user to send the verification email to
     */
    public void sendVerificationEmail(User user) {
        if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User or user ID is null or empty");
        }
        
        try {
            // Verify the user exists in the database
            Optional<User> userInDb = userRepository.findById(user.getUserId());
            if (userInDb.isEmpty()) {
                throw new IllegalArgumentException("User does not exist in the database: " + user.getUserId());
            }
            
            // Create a new token service instance
            TokenService tokenService = new TokenService(tokenRepository, this);
            
            // Create verification token and send email
            tokenService.createAccountVerificationToken(userInDb.get());
        } catch (Exception e) {
            logger.error("Error sending verification email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    /**
     * Initiates the password reset process by sending a password reset email.
     * 
     * @param email The email address of the user requesting a password reset
     * @return true if email was sent, false if the email was not found
     */
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOpt = getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        TokenService tokenService = new TokenService(tokenRepository, this);
        tokenService.createPasswordResetToken(user);
        
        return true;
    }
    
    /**
     * Resets a user's password using a token.
     * 
     * @param token The password reset token
     * @param newPassword The new password
     * @return true if password was reset successfully, false otherwise
     */
    public boolean resetPassword(String token, String newPassword) {
        TokenService tokenService = new TokenService(tokenRepository, this);
        return tokenService.resetPassword(token, newPassword);
    }
    
    /**
     * Verifies a user's account using a verification token.
     * 
     * @param token The verification token
     * @return true if verification was successful, false otherwise
     */
    public boolean verifyUserAccount(String token) {
        TokenService tokenService = new TokenService(tokenRepository, this);
        return tokenService.verifyAccountToken(token);
    }

    public void updateUser(User updatedUser) {
        userRepository.findById(updatedUser.getUserId()).ifPresent(existingUser -> {
            // Check if username is being changed to one that already exists
            if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
                if (userExists(updatedUser.getUsername())) {
                    throw new IllegalArgumentException("Username already exists");
                }
                existingUser.setUsername(updatedUser.getUsername());
            }

            // Check if email is being changed to one that already exists
            if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
                if (emailExists(updatedUser.getEmail())) {
                    throw new IllegalArgumentException("Email already exists");
                }
                existingUser.setEmail(updatedUser.getEmail());
            }

            // Update fields that can be modified
            existingUser.setProfilePictureUrl(updatedUser.getProfilePictureUrl());
            existingUser.setCountry(updatedUser.getCountry());
            existingUser.setRegion(updatedUser.getRegion());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            existingUser.setCountryPrefix(updatedUser.getCountryPrefix());
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setZipCode(updatedUser.getZipCode());
            existingUser.setActive(updatedUser.isActive());
            existingUser.setRoles(updatedUser.getRoles());
            
            // Add missing fields for firstName, lastName, and birthdate
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setBirthdate(updatedUser.getBirthdate());
            existingUser.setGender(updatedUser.getGender());

            // Only update password if a new one was provided
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(updatedUser.getPassword());
            }

            userRepository.save(existingUser);
        });
    }

    public boolean validatePassword(User user, String password) {
        if (user == null || password == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }

    /**
     * Gets all users that are members of a specific team.
     * 
     * @param teamId The ID of the team
     * @return A list of users who are members of the team
     */
    public List<User> getUsersByTeam(String teamId) {
        return userRepository.findByTeamId(teamId);
    }
}
