package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OfferCreateDTO {
    public Long applicationId;
    public LocalDate startDate;
    public LocalDate validUntil;
    public BigDecimal testScore; // non-null only when coming from Test phase
}
