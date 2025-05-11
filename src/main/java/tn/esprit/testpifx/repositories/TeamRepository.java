package tn.esprit.testpifx.repositories;

import tn.esprit.testpifx.models.Team;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Team data access.
 * Defines methods for creating, reading, updating, and deleting teams.
 */
public interface TeamRepository {
    /**
     * Saves a team to the repository.
     * If the team has no ID, a new one is generated.
     * 
     * @param team The team to save
     */
    void save(Team team);
    
    /**
     * Finds a team by its ID.
     * 
     * @param teamId The ID of the team to find
     * @return An Optional containing the team if found, or empty if not found
     */
    Optional<Team> findById(String teamId);
    
    /**
     * Finds a team by its name.
     * 
     * @param name The name of the team to find
     * @return An Optional containing the team if found, or empty if not found
     */
    Optional<Team> findByName(String name);
    
    /**
     * Returns all teams in the repository.
     * 
     * @return A list of all teams
     */
    List<Team> findAll();
    
    /**
     * Deletes a team by its ID.
     * 
     * @param teamId The ID of the team to delete
     */
    void deleteById(String teamId);
    
    /**
     * Checks if a team with the given name exists.
     * 
     * @param name The name to check
     * @return true if a team with the name exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Finds all teams created by a specific user.
     * 
     * @param userId The ID of the user who created the teams
     * @return A list of teams created by the user
     */
    List<Team> findByCreatedBy(String userId);
    
    /**
     * Finds all teams that a user is a member of.
     * 
     * @param userId The ID of the user
     * @return A list of teams the user is a member of
     */
    List<Team> findByMemberId(String userId);
    
    /**
     * Adds a user to a team.
     * 
     * @param teamId The ID of the team
     * @param userId The ID of the user to add
     * @return true if the user was added, false if they were already a member
     */
    boolean addMember(String teamId, String userId);
    
    /**
     * Removes a user from a team.
     * 
     * @param teamId The ID of the team
     * @param userId The ID of the user to remove
     * @return true if the user was removed, false if they weren't a member
     */
    boolean removeMember(String teamId, String userId);
    
    /**
     * Gets all members of a team.
     * 
     * @param teamId The ID of the team
     * @return A set of user IDs who are members of the team
     */
    Set<String> getMembers(String teamId);
    
    /**
     * Checks if a user is a member of a team.
     * 
     * @param teamId The ID of the team
     * @param userId The ID of the user
     * @return true if the user is a member, false otherwise
     */
    boolean isMember(String teamId, String userId);

    boolean existsById(String teamId);
}