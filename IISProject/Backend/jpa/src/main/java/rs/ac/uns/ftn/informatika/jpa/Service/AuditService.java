package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Postavlja trenutnog korisnika za audit log triggere
     */
    @Transactional
    public void setCurrentUserForAudit(Long userId) {
        if (userId != null) {
            try {
                jdbcTemplate.queryForObject("SELECT set_current_user_id(?)", Long.class, userId);
            } catch (Exception e) {
                // Ignoriši greške ako PL/SQL ne radi
            }
        }
    }
}
