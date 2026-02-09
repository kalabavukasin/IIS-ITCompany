package rs.ac.uns.ftn.informatika.jpa.Dto;
import jakarta.validation.constraints.NotNull;
public record AcceptOfferRequestDTO(
        @NotNull Long userId   // kandidat koji prihvata
) {}
