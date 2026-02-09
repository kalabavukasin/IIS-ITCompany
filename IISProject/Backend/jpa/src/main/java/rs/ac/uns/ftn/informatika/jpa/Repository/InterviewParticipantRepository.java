package rs.ac.uns.ftn.informatika.jpa.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.jpa.Dto.InterviewToShowDTO;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewParticipantRole;
import rs.ac.uns.ftn.informatika.jpa.Model.InterviewParticipant;

import java.util.List;

@Repository
public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipant, Long> {

    List<InterviewParticipant> findByInterview_Id(Long interviewId);

    List<InterviewParticipant> findByUser_Id(Long userId);

    List<InterviewParticipant> findByRoleOnInterview(InterviewParticipantRole role);

    @Query("SELECT p FROM InterviewParticipant p " +
            "JOIN FETCH p.user " +
            "WHERE p.interview.id = :interviewId")
    List<InterviewParticipant> findByInterviewWithUsers(@Param("interviewId") Long interviewId);

    @Query("SELECT p FROM InterviewParticipant p " +
            "WHERE p.user.id = :userId " +
            "AND p.roleOnInterview = :role")
    List<InterviewParticipant> findByUserAndRole(@Param("userId") Long userId,
                                                 @Param("role") InterviewParticipantRole role);

    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.InterviewToShowDTO(
            i.id,
            i.type,
            i.locationOrLink,
            concat(c.firstName, ' ', c.lastName),
            i.durationMinutes,
            i.status,
            i.scheduledAt
        )
        from InterviewParticipant ip
            join ip.interview i
            join i.application a
            join a.candidate c
        where ip.user.id = :interviewerId
          and ip.roleOnInterview = rs.ac.uns.ftn.informatika.jpa.Enumerations.InterviewParticipantRole.INTERVIEWER
        order by i.scheduledAt desc
        """)
    List<InterviewToShowDTO> findInterviewsToShowByInterviewerId(Long interviewerId);
    @Query("""
        select new rs.ac.uns.ftn.informatika.jpa.Dto.InterviewToShowDTO(
            i.id,
            i.type,
            i.locationOrLink,
            concat(c.firstName, ' ', c.lastName),
            i.durationMinutes,
            i.status,
            i.scheduledAt
        )
        from InterviewParticipant ip
            join ip.interview i
            join i.application a
            join a.candidate c
        where ip.user.id = :userId
          and ip.roleOnInterview = :role
        order by i.scheduledAt desc
        """)
    List<InterviewToShowDTO> findInterviewsToShowByUserAndRole(@Param("userId") Long userId,
                                                               @Param("role") InterviewParticipantRole role);

}
