package tn.esprit.testpifx.utils;

import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.UserService;

import java.time.LocalDate;
import java.util.Set;

/**
 * Utility class for initializing predefined sets of users in the application.
 * This class provides methods to create different sets of users with various roles.
 */
public class UserDataInitializer {

    private final UserService userService;

    public UserDataInitializer(UserService userService) {
        this.userService = userService;
    }

    /**
     * Initializes a basic set of users (admin, user, manager).
     * This is the default set that was previously initialized in Main.java.
     */
    public void initializeBasicUsers() {
        try {
            // Admin user
            if (!userService.userExists("admin")) {
                User admin = new User("admin", "admin", "admin@esprit.tn");
                admin.addRole(Role.ADMIN);
                admin.setProfilePictureUrl("https://randomuser.me/api/portraits/men/1.jpg");
                admin.setCountry("Tunisia");
                admin.setRegion("Tunis");
                admin.setFirstName("Ahmed");
                admin.setLastName("Ben Ali");
                admin.setPhoneNumber("22123456");
                admin.setCountryPrefix("+216");
                userService.createUser(admin);
            }

            // Regular user
            if (!userService.userExists("user")) {
                User user = new User("user", "user", "user1@esprit.tn");
                user.addRole(Role.USER);
                user.setProfilePictureUrl("https://randomuser.me/api/portraits/women/1.jpg");
                user.setCountry("Tunisia");
                user.setRegion("Sousse");
                user.setFirstName("Mariem");
                user.setLastName("Karoui");
                user.setPhoneNumber("98765432");
                user.setCountryPrefix("+216");
                userService.createUser(user);
            }

            // Manager
            if (!userService.userExists("manager")) {
                User manager = new User("manager", "manager123", "manager@esprit.tn");
                manager.addRole(Role.MANAGER);
                manager.setProfilePictureUrl("https://randomuser.me/api/portraits/men/2.jpg");
                manager.setCountry("Tunisia");
                manager.setRegion("Sfax");
                manager.setFirstName("Mohamed");
                manager.setLastName("Trabelsi");
                manager.setPhoneNumber("54123789");
                manager.setCountryPrefix("+216");
                userService.createUser(manager);
            }

            // Additional users
            if (!userService.userExists("alice")) {
                User alice = new User("alice", "alice123", "alice@esprit.tn");
                alice.addRole(Role.USER);
                alice.setProfilePictureUrl("https://randomuser.me/api/portraits/women/3.jpg");
                alice.setCountry("Tunisia");
                alice.setRegion("Monastir");
                alice.setFirstName("Alia");
                alice.setLastName("Mabrouk");
                alice.setPhoneNumber("27896541");
                alice.setCountryPrefix("+216");
                userService.createUser(alice);
            }
            if (!userService.userExists("bob")) {
                User bob = new User("bob", "bob123", "bob@esprit.tn");
                bob.addRole(Role.USER);
                bob.setProfilePictureUrl("https://randomuser.me/api/portraits/men/4.jpg");
                bob.setCountry("Tunisia");
                bob.setRegion("Bizerte");
                bob.setFirstName("Bilel");
                bob.setLastName("Gharbi");
                bob.setPhoneNumber("55423178");
                bob.setCountryPrefix("+216");
                userService.createUser(bob);
            }
            if (!userService.userExists("carol")) {
                User carol = new User("carol", "carol123", "carol@esprit.tn");
                carol.addRole(Role.MANAGER);
                carol.setProfilePictureUrl("https://randomuser.me/api/portraits/women/5.jpg");
                carol.setCountry("Tunisia");
                carol.setRegion("Nabeul");
                carol.setFirstName("Cyrine");
                carol.setLastName("Mansour");
                carol.setPhoneNumber("21543987");
                carol.setCountryPrefix("+216");
                userService.createUser(carol);
            }
            if (!userService.userExists("dave")) {
                User dave = new User("dave", "dave123", "dave@esprit.tn");
                dave.addRole(Role.USER);
                dave.setProfilePictureUrl("https://randomuser.me/api/portraits/men/6.jpg");
                dave.setCountry("Tunisia");
                dave.setRegion("Ariana");
                dave.setFirstName("Dhia");
                dave.setLastName("Sassi");
                dave.setPhoneNumber("98123456");
                dave.setCountryPrefix("+216");
                userService.createUser(dave);
            }
            if (!userService.userExists("eve")) {
                User eve = new User("eve", "eve123", "eve@esprit.tn");
                eve.addRole(Role.USER);
                eve.setProfilePictureUrl("https://randomuser.me/api/portraits/women/7.jpg");
                eve.setCountry("Tunisia");
                eve.setRegion("Gafsa");
                eve.setFirstName("Emna");
                eve.setLastName("Jouini");
                eve.setPhoneNumber("50987654");
                eve.setCountryPrefix("+216");
                userService.createUser(eve);
            }

            System.out.println("Initialized basic users");
        } catch (Exception e) {
            System.err.println("Error initializing basic users: " + e.getMessage());
        }
    }

    /**
     * Initializes an extended set of users with various roles.
     * This includes department-specific users, additional managers, and more.
     */
    public void initializeExtendedUsers() {
        try {
            // Department users - all in Tunisia with @esprit.tn emails
            createTunisianUser("cs_dept", "cs_dept123", "cs@esprit.tn", Set.of(Role.USER),
                    "https://randomuser.me/api/portraits/men/10.jpg", "Tunis", "Chokri", "Sellami");
            createTunisianUser("eng_dept", "eng_dept123", "engineering@esprit.tn", Set.of(Role.USER),
                    "https://randomuser.me/api/portraits/men/11.jpg", "Ben Arous", "Elyes", "Ghiloufi");
            createTunisianUser("math_dept", "math_dept123", "mathematics@esprit.tn", Set.of(Role.USER),
                    "https://randomuser.me/api/portraits/women/10.jpg", "Monastir", "Maha", "Messaoudi");
            createTunisianUser("physics_dept", "physics_dept123", "physics@esprit.tn", Set.of(Role.USER),
                    "https://randomuser.me/api/portraits/women/11.jpg", "Sfax", "Fatma", "Baklouti");

            // Additional managers
            createTunisianUser("it_manager", "it_manager123", "it_manager@esprit.tn", 
                    Set.of(Role.MANAGER, Role.USER), "https://randomuser.me/api/portraits/men/20.jpg", 
                    "Tunis", "Imed", "Turki");
            createTunisianUser("hr_manager", "hr_manager123", "hr_manager@esprit.tn",
                    Set.of(Role.MANAGER, Role.USER), "https://randomuser.me/api/portraits/women/20.jpg", 
                    "Ariana", "Hana", "Rezgui");
            createTunisianUser("finance_manager", "finance_manager123", "finance_manager@esprit.tn",
                    Set.of(Role.MANAGER, Role.USER), "https://randomuser.me/api/portraits/men/21.jpg", 
                    "Sousse", "Farhat", "Mejri");

            // Additional admins
            createTunisianUser("system_admin", "system_admin123", "system_admin@esprit.tn", 
                    Set.of(Role.ADMIN, Role.USER), "https://randomuser.me/api/portraits/men/30.jpg", 
                    "Tunis", "Skander", "Abidi");

            // Regular users
            String[] regions = {"Tunis", "Ariana", "Ben Arous", "Manouba", "Bizerte", "Sousse", "Sfax", "Nabeul", "Monastir", "Gabès"};
            String[] firstNames = {"Amine", "Sami", "Nadia", "Leila", "Omar", "Rania", "Karim", "Yasmine", "Youssef", "Sarra"};
            String[] lastNames = {"Ben Ahmed", "Chaabane", "Dridi", "Koubaa", "Mrad", "Nasri", "Riahi", "Talbi", "Zaouali", "Oueslati"};
            
            for (int i = 2; i <= 10; i++) {
                // Alternate between male and female portraits
                String gender = i % 2 == 0 ? "men" : "women";
                int portraitNumber = 40 + i;
                String firstNameIndex = (i % firstNames.length) + "";
                String lastNameIndex = (i % lastNames.length) + "";
                String regionIndex = (i % regions.length) + "";
                
                createTunisianUser("user" + i, "password" + i, "user" + i + "@esprit.tn", 
                        Set.of(Role.USER), "https://randomuser.me/api/portraits/" + gender + "/" + portraitNumber + ".jpg",
                        regions[i % regions.length], firstNames[i % firstNames.length], lastNames[i % lastNames.length]);
            }

            System.out.println("Initialized extended users");
        } catch (Exception e) {
            System.err.println("Error initializing extended users: " + e.getMessage());
        }
    }

    /**
     * Initializes a set of test users with various combinations of roles.
     * Useful for testing role-based access control.
     */
    public void initializeTestUsers() {
        try {
            // Users with different role combinations
            createTunisianUser("admin_manager", "test123", "admin_manager@esprit.tn", 
                    Set.of(Role.ADMIN, Role.MANAGER), "https://randomuser.me/api/portraits/men/50.jpg",
                    "Tunis", "Adel", "Mejri");
            createTunisianUser("admin_user", "test123", "admin_user@esprit.tn",
                    Set.of(Role.ADMIN, Role.USER), "https://randomuser.me/api/portraits/women/50.jpg",
                    "Monastir", "Amina", "Lahmar");
            createTunisianUser("manager_user", "test123", "manager_user@esprit.tn",
                    Set.of(Role.MANAGER, Role.USER), "https://randomuser.me/api/portraits/men/51.jpg",
                    "Sfax", "Majdi", "Ouertani");
            createTunisianUser("all_roles", "test123", "all_roles@esprit.tn",
                    Set.of(Role.ADMIN, Role.MANAGER, Role.USER), "https://randomuser.me/api/portraits/women/51.jpg",
                    "Sousse", "Asma", "Laabidi");

            // Inactive users - still with Tunisia data
            User inactiveUser = new User("inactive_user", "test123", "inactive@esprit.tn");
            inactiveUser.setActive(false);
            inactiveUser.setProfilePictureUrl("https://randomuser.me/api/portraits/men/60.jpg");
            inactiveUser.setCountry("Tunisia");
            inactiveUser.setRegion("Gabès");
            inactiveUser.setFirstName("Ismail");
            inactiveUser.setLastName("Ferchichi");
            inactiveUser.setPhoneNumber("20123456");
            inactiveUser.setCountryPrefix("+216");
            if (!userService.userExists(inactiveUser.getUsername())) {
                userService.createUser(inactiveUser);
                // Need to disable after creation since createUser sets active to true
                userService.disableUser(inactiveUser.getUserId());
            }

            // Additional users from multiple regions of Tunisia
            createTunisianUser("kairouan_user", "test123", "kairouan@esprit.tn",
                    Set.of(Role.USER), "https://randomuser.me/api/portraits/men/61.jpg",
                    "Kairouan", "Khaled", "Nasri");
            createTunisianUser("mahdia_user", "test123", "mahdia@esprit.tn",
                    Set.of(Role.USER), "https://randomuser.me/api/portraits/women/61.jpg",
                    "Mahdia", "Mouna", "Hamdi");
            createTunisianUser("kasserine_user", "test123", "kasserine@esprit.tn",
                    Set.of(Role.USER), "https://randomuser.me/api/portraits/men/62.jpg",
                    "Kasserine", "Kamel", "Chebbi");
            createTunisianUser("jendouba_user", "test123", "jendouba@esprit.tn",
                    Set.of(Role.USER), "https://randomuser.me/api/portraits/women/62.jpg",
                    "Jendouba", "Jihen", "Haddad");
            createTunisianUser("tozeur_user", "test123", "tozeur@esprit.tn",
                    Set.of(Role.USER), "https://randomuser.me/api/portraits/men/63.jpg",
                    "Tozeur", "Tarek", "Bouazizi");

            System.out.println("Initialized test users");
        } catch (Exception e) {
            System.err.println("Error initializing test users: " + e.getMessage());
        }
    }

    /**
     * Helper method to create a Tunisian user with the specified attributes if it doesn't already exist.
     */
    private void createTunisianUser(String username, String password, String email, 
                                   Set<Role> roles, String profilePictureUrl,
                                   String region, String firstName, String lastName) {
        if (!userService.userExists(username)) {
            User user = new User(username, password, email);
            user.setRoles(roles);
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                user.setProfilePictureUrl(profilePictureUrl);
            }
            
            // Set Tunisia as country and other personal details
            user.setCountry("Tunisia");
            user.setRegion(region);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(generateRandomTunisianPhoneNumber());
            user.setCountryPrefix("+216");
            
            // Maybe add some address data
            user.setAddress("123 Habib Bourguiba Ave.");
            user.setZipCode("2" + (1000 + (int)(Math.random() * 9000)));
            
            // Set random birth date between 1970 and 2000
            int year = 1970 + (int)(Math.random() * 30);
            int month = 1 + (int)(Math.random() * 12);
            int day = 1 + (int)(Math.random() * 28);
            user.setBirthdate(LocalDate.of(year, month, day));
            
            // Set gender based on portrait URL (simple heuristic)
            user.setGender(profilePictureUrl.contains("/men/") ? "Male" : "Female");
            
            userService.createUser(user);
        }
    }

    /**
     * Helper method to create a user with the specified roles if it doesn't already exist.
     */
    private void createUserIfNotExists(String username, String password, String email, Set<Role> roles) {
        createUserIfNotExists(username, password, email, roles, null);
    }

    /**
     * Helper method to create a user with the specified roles and profile picture if it doesn't already exist.
     */
    private void createUserIfNotExists(String username, String password, String email, Set<Role> roles, String profilePictureUrl) {
        if (!userService.userExists(username)) {
            User user = new User(username, password, email);
            user.setRoles(roles);
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                user.setProfilePictureUrl(profilePictureUrl);
            }
            userService.createUser(user);
        }
    }
    
    /**
     * Generates a random Tunisian phone number starting with 2, 5, 9 or 4.
     */
    private String generateRandomTunisianPhoneNumber() {
        // Tunisian mobile numbers start with 2, 5, or 9
        String[] prefixes = {"2", "5", "9", "4"};
        String prefix = prefixes[(int)(Math.random() * prefixes.length)];
        
        // Generate 7 random digits
        StringBuilder number = new StringBuilder(prefix);
        for (int i = 0; i < 7; i++) {
            number.append((int)(Math.random() * 10));
        }
        
        return number.toString();
    }
}
