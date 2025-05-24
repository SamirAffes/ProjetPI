package tn.esprit.testpifx.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
/**
 *** Mustapha Sifaoui
/**
 * Represents a team or group in the system.
 * Teams can have multiple members (users) and are managed by admins or managers.
 */
public class Team {
    private String teamId;
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty createdBy = new SimpleStringProperty(); // User ID of the creator
    private Set<String> memberIds = new HashSet<>(); // Set of user IDs who are members of this team

    /**
     * Default constructor
     */
    public Team() {
    }

    /**
     * Constructor with name and description
     * 
     * @param name The name of the team
     * @param description The description of the team
     * @param createdBy The ID of the user who created the team
     */
    public Team(String name, String description, String createdBy) {
        this.teamId = UUID.randomUUID().toString();
        this.name.set(name);
        this.description.set(description);
        this.createdBy.set(createdBy);
    }

    /**
     * Gets the team ID
     * 
     * @return The team ID
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * Sets the team ID
     * 
     * @param teamId The team ID to set
     */
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    /**
     * Gets the team name
     * 
     * @return The team name
     */
    public String getName() {
        return name.get();
    }

    /**
     * Gets the name property
     * 
     * @return The name property
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the team name
     * 
     * @param name The team name to set
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Gets the team description
     * 
     * @return The team description
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * Gets the description property
     * 
     * @return The description property
     */
    public StringProperty descriptionProperty() {
        return description;
    }

    /**
     * Sets the team description
     * 
     * @param description The team description to set
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * Gets the ID of the user who created the team
     * 
     * @return The creator's user ID
     */
    public String getCreatedBy() {
        return createdBy.get();
    }

    /**
     * Gets the createdBy property
     * 
     * @return The createdBy property
     */
    public StringProperty createdByProperty() {
        return createdBy;
    }

    /**
     * Sets the ID of the user who created the team
     * 
     * @param createdBy The creator's user ID to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy.set(createdBy);
    }

    /**
     * Gets the set of member user IDs
     * 
     * @return The set of member user IDs
     */
    public Set<String> getMemberIds() {
        return new HashSet<>(memberIds);
    }

    /**
     * Sets the set of member user IDs
     * 
     * @param memberIds The set of member user IDs to set
     */
    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = new HashSet<>(memberIds);
    }

    /**
     * Adds a member to the team
     * 
     * @param userId The ID of the user to add
     * @return true if the user was added, false if they were already a member
     */
    public boolean addMember(String userId) {
        return memberIds.add(userId);
    }

    /**
     * Removes a member from the team
     * 
     * @param userId The ID of the user to remove
     * @return true if the user was removed, false if they weren't a member
     */
    public boolean removeMember(String userId) {
        return memberIds.remove(userId);
    }

    /**
     * Checks if a user is a member of the team
     * 
     * @param userId The ID of the user to check
     * @return true if the user is a member, false otherwise
     */
    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }

    /**
     * Returns a string representation of the team
     * 
     * @return A string representation of the team
     */
    @Override
    public String toString() {
        return "Team{" +
                "teamId='" + teamId + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", createdBy='" + getCreatedBy() + '\'' +
                ", memberCount=" + memberIds.size() +
                '}';
    }
}