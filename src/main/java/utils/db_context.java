package utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;


@Slf4j
public class db_context{
    private final Dotenv dotenv = Dotenv.load();
    private final String USER = dotenv.get("DB_USER");
    private final String PASSWORD = dotenv.get("DB_PASSWORD");
    private final String URL = dotenv.get("DB_URL");

    private static volatile db_context instance;

    @Getter
    private Connection conn;

    private db_context() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            log.info("Connected to database");
        } catch (SQLException e) {
            log.error(e.getMessage());
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


}
