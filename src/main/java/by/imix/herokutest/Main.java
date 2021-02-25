package by.imix.herokutest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@ConfigurationProperties
@Controller
@SpringBootApplication
public class Main {

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.user}")
    private String userName;
    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping("/")
    String index() {
        return "index";
    }

    @RequestMapping("/hello")
    String hello() {
        return "hello Miha";
    }

    @RequestMapping("/createdb")
    String db(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (" +
                    " task_id SERIAL PRIMARY KEY,\n" +
                    "    title VARCHAR(255) NOT NULL,\n" +
                    "    start_date DATE,\n" +
                    "    due_date DATE,\n" +
                    "    status SMALLINT NOT NULL,\n" +
                    "    priority SMALLINT NOT NULL,\n" +
                    "    description TEXT,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                    ")");
            model.put("message", "Таблица успешно создана");
            return "dbcreate";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping("/readdb")
    String dbRead(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM ticks");

            ArrayList<String> output = new ArrayList<String>();
            while (rs.next()) {
                output.add("Read from DB: " + rs.getString("title") + " Статус: " + rs.getString("status"));
            }

            model.put("records", output);
            return "db";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping("/insticket")
    boolean insertTicket(@RequestParam String newTick, @RequestParam String state) {
      try (Connection connection = dataSource.getConnection()) {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("INSERT INTO ticks (title,start_date,status,priority) VALUES ('"+newTick+"',now(), '"+state+"', '1')");
        return true;
      } catch (Exception e) {

        return false;
      }
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        } else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(userName);
            config.setPassword(password);
            return new HikariDataSource(config);
        }
    }

}
