package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.time.LocalDate;

public record OfferCardDTO(
        Long offerId,
        String status,
        LocalDate startDate,
        Long applicationId,
        String requestName,
        String requestDescription
) {}
