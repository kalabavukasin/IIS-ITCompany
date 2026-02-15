package rs.ac.uns.ftn.informatika.jpa.Util;

import org.springframework.security.core.context.SecurityContextHolder;
import rs.ac.uns.ftn.informatika.jpa.Model.CustomUserDetails;
import rs.ac.uns.ftn.informatika.jpa.Model.User;

public class SecurityUtils {

    public static User getCurrentUser() {
        CustomUserDetails details = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return details.getUser();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
