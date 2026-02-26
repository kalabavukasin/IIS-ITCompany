package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.OffsetDateTime;
import java.util.List;

public class PostingApplicantDTO {

    public Long applicationId;
    public String status;
    public String currentPhase;

    public Long candidateId;
    public String candidateName;
    public String cvDownloadUrl;

    public Integer autoAiScore;
    public String autoAiScoreNote;
    public OffsetDateTime autoAiScoredAt;
    public Integer bulkAiScore;
    public String bulkAiScoreNote;
    public OffsetDateTime bulkAiScoredAt;

    public List<String> phases;

    public PostingApplicantDTO(
            Long applicationId,
            String status,
            String currentPhase,
            Long candidateId,
            String candidateName,
            Integer autoAiScore,
            String autoAiScoreNote,
            OffsetDateTime autoAiScoredAt,
            Integer bulkAiScore,
            String bulkAiScoreNote,
            OffsetDateTime bulkAiScoredAt) {
        this.applicationId = applicationId;
        this.status = status;
        this.currentPhase = currentPhase;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.autoAiScore = autoAiScore;
        this.autoAiScoreNote = autoAiScoreNote;
        this.autoAiScoredAt = autoAiScoredAt;
        this.bulkAiScore = bulkAiScore;
        this.bulkAiScoreNote = bulkAiScoreNote;
        this.bulkAiScoredAt = bulkAiScoredAt;
    }
}
