package utils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;


@Slf4j
public class DB_Context{
    private final Dotenv dotenv = Dotenv.load();
    private final String USER = dotenv.get("DB_USER");
    private final String PASSWORD = dotenv.get("DB_PASSWORD");
    private final String URL = dotenv.get("DB_URL");

    private static volatile DB_Context instance;

    @Getter
    private Connection conn;

    private DB_Context() {

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            log.info("Connected to database");
        } catch (SQLException e) {
            log.error("Unable to connect to DB: " + e.getMessage(), e);
            throw new IllegalStateException("Database connection failed", e);
        }

    }


    public static DB_Context getInstance() {
        DB_Context temp = instance;
        if (temp == null) {
            synchronized (DB_Context.class) {
                temp = instance;
                if (temp == null) instance = temp= new DB_Context();
            }
        }
        return temp;
    }


}
