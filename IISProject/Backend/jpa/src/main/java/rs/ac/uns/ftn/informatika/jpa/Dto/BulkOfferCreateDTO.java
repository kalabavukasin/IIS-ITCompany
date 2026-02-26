package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.LocalDate;
import java.util.List;

public class BulkOfferCreateDTO {
    public List<Long> applicationIds;
    public LocalDate startDate;
    public LocalDate validUntil;
    public List<BulkTestScoreEntryDTO> testScores;
}
