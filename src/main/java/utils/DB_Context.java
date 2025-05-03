package utils;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class db_context{
    private static final Logger logger = LoggerFactory.getLogger(db_context.class);
    
    private final Dotenv dotenv = Dotenv.load();
    private final String USER = dotenv.get("DB_USER");
    private final String PASSWORD = dotenv.get("DB_PASSWORD");
    private final String URL = dotenv.get("DB_URL");

    private static volatile db_context instance;

    @Getter
    private Connection connection;

    private db_context() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Connected to database");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static db_context getInstance() {
        db_context temp = instance;
        if (temp == null) {
            synchronized (db_context.class) {
                temp = instance;
                if (temp == null) instance = temp= new db_context();
            }
        }
        return temp;
    }

    public Connection getConn() {
        return connection;
    }
}
