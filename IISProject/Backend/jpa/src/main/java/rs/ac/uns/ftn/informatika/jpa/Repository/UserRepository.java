package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Role;
import rs.ac.uns.ftn.informatika.jpa.Model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    //List<User> findByIsActiveFalse();
    boolean existsByEmail(String email);
    List<User> findByRoleIn(List<Role> roles);
    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.isActive = true")
    List<User> findActiveByRoleIn(@Param("roles") List<Role> roles);

    @Query("""
        select u
        from User u
        where u.role = rs.ac.uns.ftn.informatika.jpa.Enumerations.Role.HIRING_MANAGER
          and (u.isActive = true or u.isActive is null)
        order by (
            select count(r)
            from Requestion r
            where r.hiringManager = u
              and r.status <> rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus.CLOSED
        ) asc,
        u.id asc
    """)
    List<User> findHiringManagersOrderedByOpenRequestions(Pageable pageable);
}
