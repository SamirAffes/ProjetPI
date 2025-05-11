package tn.esprit.testpifx.utils;

import tn.esprit.testpifx.models.Team;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for initializing predefined sets of teams in the application.
 * This class provides methods to create different sets of teams with various members.
 */
public class TeamDataInitializer {

    private final TeamService teamService;
    private final UserService userService;

    /**
     * Constructor
     * 
     * @param teamService The team service to use
     * @param userService The user service to use
     */
    public TeamDataInitializer(TeamService teamService, UserService userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    /**
     * Initializes a basic set of teams.
     */
    public void initializeBasicTeams() {
        try {
            User admin = userService.findByUsername("admin")
                    .orElseThrow(() -> new IllegalStateException("Admin user not found"));
            
            // Find another user to add as a team member (ensuring at least 2 members)
            User secondUser = userService.findByUsername("user")
                    .orElseGet(() -> userService.findByUsername("manager")
                    .orElseThrow(() -> new IllegalStateException("No second user found for team")));

            // Only create if not exists
            if (!teamService.getTeamByName("Esprit Development").isPresent()) {
                Team team = new Team("Esprit Development", "Team responsible for software development at Esprit", admin.getUserId());
                team.addMember(admin.getUserId());
                
                // Add second member to ensure at least 2 members
                team.addMember(secondUser.getUserId());
                
                teamService.getTeamRepository().save(team);
                System.out.println("Initialized basic teams with members: admin and " + secondUser.getUsername());
            } else {
                // For existing teams, ensure they have at least 2 members
                Team team = teamService.getTeamByName("Esprit Development").get();
                if (team.getMemberIds().size() < 2) {
                    // Add second member if needed
                    if (!team.isMember(secondUser.getUserId())) {
                        team.addMember(secondUser.getUserId());
                        teamService.getTeamRepository().save(team);
                        System.out.println("Added " + secondUser.getUsername() + " to existing team: Esprit Development");
                    }
                }
                System.out.println("Development Team already exists, ensured it has at least 2 members");
            }
        } catch (Exception e) {
            System.err.println("Error initializing basic teams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes an extended set of teams with various members.
     */
    public void initializeDepartmentTeams() {
        try {
            User admin = userService.findByUsername("admin")
                    .orElseThrow(() -> new IllegalStateException("Admin user not found"));
            User manager = userService.findByUsername("manager")
                    .orElseThrow(() -> new IllegalStateException("Manager user not found"));
            User regularUser = userService.findByUsername("user")
                    .orElseThrow(() -> new IllegalStateException("Regular user not found"));

            // Create Tunisian department teams
            createTunisianTeam("Esprit Engineering", "Engineering department of Esprit university", admin, 
                    Arrays.asList("manager", "user", "alice", "bob"));
            
            createTunisianTeam("Esprit Business", "Business and management department", manager, 
                    Arrays.asList("carol", "dave", "eve"));
            
            createTunisianTeam("Esprit Computer Science", "Computer science and IT department", admin, 
                    Arrays.asList("user", "cs_dept", "eng_dept", "system_admin"));
            
            createTunisianTeam("Esprit Mathematics", "Mathematics and statistics department", manager, 
                    Arrays.asList("math_dept", "physics_dept"));
            
            createTunisianTeam("Esprit Student Clubs", "Student activities coordination team", regularUser, 
                    Arrays.asList("alice", "bob", "user2", "user3"));

            System.out.println("Initialized department teams");
        } catch (Exception e) {
            System.err.println("Error initializing department teams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes project teams.
     */
    public void initializeProjectTeams() {
        try {
            User admin = userService.findByUsername("admin")
                    .orElseThrow(() -> new IllegalStateException("Admin user not found"));
            User manager = userService.findByUsername("manager")
                    .orElseThrow(() -> new IllegalStateException("Manager user not found"));
            User regularUser = userService.findByUsername("user")
                    .orElseThrow(() -> new IllegalStateException("Regular user not found"));

            // Create Tunisian project teams
            createTunisianTeam("Project Carthage", "Historic digital preservation initiative", admin, 
                    Arrays.asList("manager", "user", "alice", "bob", "carol"));
            
            createTunisianTeam("Project Medina", "Urban development and smart cities initiative", manager, 
                    Arrays.asList("it_manager", "hr_manager", "dave", "eve", "user2"));
            
            createTunisianTeam("Project Jasmine", "Technology innovation accelerator", admin, 
                    Arrays.asList("finance_manager", "user3", "user4", "cs_dept", "math_dept"));
            
            createTunisianTeam("Project Sahara", "Renewable energy development", manager, 
                    Arrays.asList("eng_dept", "physics_dept", "user5", "user6"));
            
            createTunisianTeam("Project Olive", "Agricultural technology advancement", admin, 
                    Arrays.asList("manager_user", "admin_user", "kairouan_user", "mahdia_user"));
            
            createTunisianTeam("Project Sidi Bou", "Tourism technology platform", regularUser, 
                    Arrays.asList("kasserine_user", "jendouba_user", "tozeur_user", "user7"));
            
            createTunisianTeam("Project Star", "Astronomy and space research", admin, 
                    Arrays.asList("user8", "user9", "user10", "admin_manager"));
            
            createTunisianTeam("Project Carthage Gate", "Cybersecurity infrastructure", manager, 
                    Arrays.asList("system_admin", "all_roles", "manager", "user"));

            System.out.println("Initialized project teams");
        } catch (Exception e) {
            System.err.println("Error initializing project teams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a Tunisian-themed team if it doesn't already exist
     *
     * @param name The name of the team
     * @param description The description of the team
     * @param creator The user creating the team
     * @param memberUsernames The usernames of the users to add to the team
     */
    private void createTunisianTeam(String name, String description, User creator, List<String> memberUsernames) {
        try {
            Team team;
            Optional<Team> existingTeam = teamService.getTeamByName(name);
            
            if (!existingTeam.isPresent()) {
                // Create new team
                team = new Team(name, description, creator.getUserId());
                team.addMember(creator.getUserId());
                teamService.getTeamRepository().save(team);
                System.out.println("Created team: " + name + " with ID: " + team.getTeamId());
            } else {
                // Use existing team
                team = existingTeam.get();
                System.out.println("Team " + name + " already exists with ID: " + team.getTeamId());
            }
            
            // Clear existing members (except creator) to avoid duplicates or stale data
            for (String memberId : team.getMemberIds()) {
                if (!memberId.equals(creator.getUserId())) {
                    try {
                        teamService.removeMember(team.getTeamId(), memberId, creator);
                        System.out.println("Removed existing member: " + memberId + " from team: " + name);
                    } catch (Exception e) {
                        System.out.println("Could not remove member: " + e.getMessage());
                    }
                }
            }
            
            // Add the creator if not already a member (should be handled by team creation but just in case)
            if (!team.isMember(creator.getUserId())) {
                team.addMember(creator.getUserId());
                teamService.getTeamRepository().save(team);
                System.out.println("Added creator: " + creator.getUsername() + " (ID: " + creator.getUserId() + ") to team: " + name);
            }
            
            // Add team members with better error handling and logging
            int addedCount = 0;
            for (String username : memberUsernames) {
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    try {
                        if (!team.isMember(user.getUserId())) {
                            team.addMember(user.getUserId());
                            teamService.getTeamRepository().save(team);
                            System.out.println("Added member: " + username + " (ID: " + user.getUserId() + ") to team: " + name);
                            addedCount++;
                        } else {
                            System.out.println("User " + username + " is already a member of team: " + name);
                        }
                    } catch (Exception e) {
                        System.err.println("Error adding " + username + " to team " + name + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("User not found: " + username + ", cannot add to team: " + name);
                }
            }
            
            System.out.println("Successfully added " + addedCount + " members to team: " + name);
            
            // Verify team members in database
            System.out.println("Final member count for team " + name + ": " + team.getMemberIds().size());
        } catch (Exception e) {
            System.err.println("Error creating team " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create a team if it doesn't already exist.
     * 
     * @param name The name of the team
     * @param description The description of the team
     * @param creator The user creating the team
     * @return The created or existing team
     */
    private Team createTeamIfNotExists(String name, String description, User creator) {
        // Check if the team already exists
        return teamService.getTeamByName(name).orElseGet(() -> {
            // Create a new team
            Team team = new Team(name, description, creator.getUserId());
            teamService.createTeam(team, creator);
            System.out.println("Team created: " + name);
            return team;
        });
    }

    /**
     * Helper method to add members to a team.
     * 
     * @param team The team to add members to
     * @param usernames The usernames of the users to add
     * @param currentUser The user performing the action
     */
    private void addMembersToTeam(Team team, List<String> usernames, User currentUser) {
        for (String username : usernames) {
            userService.findByUsername(username).ifPresent(user -> {
                try {
                    if (!teamService.isTeamMember(team.getTeamId(), user.getUserId())) {
                        teamService.addMember(team.getTeamId(), user.getUserId(), currentUser);
                        System.out.println("Added " + username + " to team " + team.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error adding " + username + " to team " + team.getName() + ": " + e.getMessage());
                }
            });
        }
    }
}