package models;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String userId;
    private String username;
    private String password;
    private String email;
    private boolean active;
    private Set<Role> roles = new HashSet<>();

    // Constructors, getters, setters
    public User() {}

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = true;
    }

    // Add other methods like addRole, removeRole, hasRole, etc.
}
