package utils;

import entities.User;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserContext {
    private static final Logger logger = LoggerFactory.getLogger(UserContext.class);
    private static volatile UserContext instance;
    
    @Getter
    private User currentUser;
    
    private UserContext() {
        // Private constructor to prevent direct instantiation
    }
    
    public static UserContext getInstance() {
        UserContext temp = instance;
        if (temp == null) {
            synchronized (UserContext.class) {
                temp = instance;
                if (temp == null) {
                    instance = temp = new UserContext();
                }
            }
        }
        return temp;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        logger.info("Current user set: {}", user != null ? user.getUsername() : "null");
    }
    
    public void clearCurrentUser() {
        this.currentUser = null;
        logger.info("Current user cleared");
    }
    
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }
}
