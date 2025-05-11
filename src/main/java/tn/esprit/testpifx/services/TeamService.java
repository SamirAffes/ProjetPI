package tn.esprit.testpifx.services;

import tn.esprit.testpifx.models.Permission;
import tn.esprit.testpifx.models.Team;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.repositories.TeamRepository;
import tn.esprit.testpifx.repositories.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for team management.
 * Provides business logic for creating, reading, updating, and deleting teams,
 * as well as managing team members.
 */
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = Objects.requireNonNull(teamRepository, "TeamRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public TeamRepository getTeamRepository() {
        return this.teamRepository;
    }

    public void createTeam(Team team, User creator) {
        Objects.requireNonNull(team, "Team cannot be null");
        Objects.requireNonNull(creator, "Creator cannot be null");

        // Modified permission check to allow users to create teams for themselves from profile view
        // Check if the user has the permission OR if this is a test team being created from profile view
        boolean isTestTeamForProfile = team.getName() != null && 
            team.getName().startsWith("Test Team for ") && 
            team.getName().contains(creator.getUsername());
            
        if (!creator.hasPermission(Permission.TEAM_CREATE) && !isTestTeamForProfile) {
            throw new IllegalArgumentException("User does not have permission to create teams");
        }

        if (teamRepository.existsByName(team.getName())) {
            throw new IllegalArgumentException("Team name already exists");
        }

        if (team.getCreatedBy() == null || team.getCreatedBy().isEmpty()) {
            team.setCreatedBy(creator.getUserId());
        }

        // Add the creator directly to the team's member list before saving
        team.addMember(creator.getUserId());
        
        // Save the team with the creator already added as a member
        teamRepository.save(team);
    }

    public Optional<Team> getTeamById(String teamId) {
        return teamRepository.findById(teamId);
    }

    public Optional<Team> getTeamByName(String name) {
        return teamRepository.findByName(name);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public List<Team> getTeamsByCreator(String userId) {
        return teamRepository.findByCreatedBy(userId);
    }

    public List<Team> getTeamsByMember(String userId) {
        return teamRepository.findByMemberId(userId);
    }

    public void updateTeam(Team team, User user) {
        Objects.requireNonNull(team, "Team cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        Team existingTeam = teamRepository.findById(team.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!existingTeam.getCreatedBy().equals(user.getUserId()) &&
                !user.hasPermission(Permission.TEAM_UPDATE)) {
            throw new IllegalArgumentException("User does not have permission to update this team");
        }

        if (!existingTeam.getName().equals(team.getName()) &&
                teamRepository.existsByName(team.getName())) {
            throw new IllegalArgumentException("Team name already exists");
        }

        existingTeam.setName(team.getName());
        existingTeam.setDescription(team.getDescription());
        teamRepository.save(existingTeam);
    }

    public void deleteTeam(String teamId, User user) {
        Objects.requireNonNull(user, "User cannot be null");

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        if (!team.getCreatedBy().equals(user.getUserId()) &&
                !user.hasPermission(Permission.TEAM_DELETE)) {
            throw new IllegalArgumentException("User does not have permission to delete this team");
        }

        // Just delete the team - the team_members table entries will be automatically deleted
        // due to the foreign key constraint with ON DELETE CASCADE
        teamRepository.deleteById(teamId);
        
        // No longer need to update user objects since teamIds was removed from User model
    }

    public boolean addMember(String teamId, String userId, User currentUser) {
        Objects.requireNonNull(currentUser, "Current user cannot be null");

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!team.getCreatedBy().equals(currentUser.getUserId()) &&
                !currentUser.hasPermission(Permission.TEAM_MEMBER_ADD)) {
            throw new IllegalArgumentException("User does not have permission to add members to this team");
        }

        // Add the user to the team in the database through the team_members table
        return teamRepository.addMember(teamId, userId);
        // No longer need to call user.addTeam() since teamIds was removed from User model
    }

    public boolean removeMember(String teamId, String userId, User currentUser) {
        Objects.requireNonNull(currentUser, "Current user cannot be null");

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!team.getCreatedBy().equals(currentUser.getUserId()) &&
                !currentUser.hasPermission(Permission.TEAM_MEMBER_REMOVE)) {
            throw new IllegalArgumentException("User does not have permission to remove members from this team");
        }

        // Remove the user from the team in the database through the team_members table
        return teamRepository.removeMember(teamId, userId);
        // No longer need to call user.removeTeam() since teamIds was removed from User model
    }

    public Set<String> getTeamMembers(String teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("Team not found");
        }
        return teamRepository.getMembers(teamId);
    }

    public boolean isTeamMember(String teamId, String userId) {
        return teamRepository.isMember(teamId, userId);
    }
}