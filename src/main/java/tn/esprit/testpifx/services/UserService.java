package tn.esprit.testpifx.services;

import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private final boolean initializeAdmin;

    public UserService(UserRepository userRepository) {
        this(userRepository, true);
    }

    public UserService(UserRepository userRepository, boolean initializeAdmin) {
        this.userRepository = userRepository;
        this.initializeAdmin = initializeAdmin;
        if (initializeAdmin) {
            initializeAdminUser();
        }
    }

    public void initializeAdminUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "admin", "admin@university.edu");
            admin.getRoles().add(Role.ADMIN);
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
        return userRepository.existsByEmail(email);
    }

    public void createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        userRepository.save(user);
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
