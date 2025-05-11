package tn.esprit.testpifx.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class User {
    private String userId;
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private String password;
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty profilePictureUrl = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty();
    private final StringProperty region = new SimpleStringProperty(); // New region field for governorates/states
    private final StringProperty phoneNumber = new SimpleStringProperty();
    private final StringProperty countryPrefix = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty zipCode = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> birthdate = new SimpleObjectProperty<>();
    private Set<Role> roles = new HashSet<>();
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    // Removed teamIds field as it's redundant with the team_members table

    public User() {}

    public User(String username, String password, String email) {
        this.username.set(username);
        this.password = password;
        this.email.set(email);
        this.firstName.set("");
        this.lastName.set("");
        this.profilePictureUrl.set("");
        this.country.set("");
        this.region.set(""); // Initialize the region field
        this.phoneNumber.set("");
        this.countryPrefix.set("");
        this.address.set("");
        this.zipCode.set("");
        this.gender.set("");
        this.birthdate.set(null);
        this.active.set(true);
        this.roles.add(Role.USER);
    }

    // User ID
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Username with property support
    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    // First Name with property support
    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    // Last Name with property support
    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    
    // Full Name (convenience method)
    public String getFullName() {
        String first = firstName.get() != null ? firstName.get() : "";
        String last = lastName.get() != null ? lastName.get() : "";
        if (first.isEmpty() && last.isEmpty()) {
            return getUsername();
        }
        return (first + " " + last).trim();
    }

    // Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Email with property support
    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }
    
    // Birthdate with property support
    public LocalDate getBirthdate() {
        return birthdate.get();
    }
    
    public ObjectProperty<LocalDate> birthdateProperty() {
        return birthdate;
    }
    
    public void setBirthdate(LocalDate birthdate) {
        this.birthdate.set(birthdate);
    }

    // Profile Picture URL with property support
    public String getProfilePictureUrl() {
        return profilePictureUrl.get();
    }

    public StringProperty profilePictureUrlProperty() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl.set(profilePictureUrl);
    }

    // Country with property support
    public String getCountry() {
        return country.get();
    }

    public StringProperty countryProperty() {
        return country;
    }

    public void setCountry(String country) {
        this.country.set(country);
    }

    // Region with property support
    public String getRegion() {
        return region.get();
    }

    public StringProperty regionProperty() {
        return region;
    }

    public void setRegion(String region) {
        this.region.set(region);
    }

    // Phone Number with property support
    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    // Country Prefix with property support
    public String getCountryPrefix() {
        return countryPrefix.get();
    }

    public StringProperty countryPrefixProperty() {
        return countryPrefix;
    }

    public void setCountryPrefix(String countryPrefix) {
        this.countryPrefix.set(countryPrefix);
    }

    // Address with property support
    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    // Zip Code with property support
    public String getZipCode() {
        return zipCode.get();
    }

    public StringProperty zipCodeProperty() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode.set(zipCode);
    }

    // Gender with property support
    public String getGender() {
        return gender.get();
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    // Active status
    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    // Roles
    public Set<Role> getRoles() {
        return roles;
    }

    public String getRolesAsString() {
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(", "));
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean hasPermission(Permission permission) {
        return roles.stream().anyMatch(role -> role.hasPermission(permission));
    }

    @Override
    public String toString() {
        if (!getFirstName().isEmpty() || !getLastName().isEmpty()) {
            return getFullName() + " (" + username.get() + ")";
        }
        return username.get();
    }
}
