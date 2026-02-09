package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


public class PlSqlInitializationService implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(PlSqlInitializationService.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initializing PL/SQL components...");
        
        try {
            // Pokretanje PL/SQL skriptova
            executeSqlScript("sql/plsql_types.sql");
            executeSqlScript("sql/plsql_functions.sql");
            executeSqlScript("sql/plsql_triggers.sql");
            executeSqlScript("sql/plsql_indexes.sql");
            executeSqlScript("sql/plsql_report.sql");
            
            logger.info("PL/SQL components initialized successfully!");
            
        } catch (Exception e) {
            logger.severe("Error initializing PL/SQL components: " + e.getMessage());
            // Ne prekidamo pokretanje aplikacije ako PL/SQL ne radi
        }
    }

    private void executeSqlScript(String scriptPath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            String script = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // Podeli skript na pojedinačne komande
            String[] commands = script.split(";");
            
            for (String command : commands) {
                command = command.trim();
                if (!command.isEmpty() && !command.startsWith("--")) {
                    try {
                        jdbcTemplate.execute(command);
                    } catch (Exception e) {
                        // Ignoriši greške za komande koje već postoje (CREATE IF NOT EXISTS)
                        if (!e.getMessage().contains("already exists") && 
                            !e.getMessage().contains("duplicate key")) {
                            logger.warning("Warning executing command: " + e.getMessage());
                        }
                    }
                }
            }
            
        } catch (IOException e) {
            logger.warning("Could not load SQL script: " + scriptPath + " - " + e.getMessage());
        }
    }
}
