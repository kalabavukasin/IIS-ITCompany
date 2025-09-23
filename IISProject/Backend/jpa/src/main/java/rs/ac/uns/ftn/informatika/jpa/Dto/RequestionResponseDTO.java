package rs.ac.uns.ftn.informatika.jpa.Dto;

import rs.ac.uns.ftn.informatika.jpa.Enumerations.RequestionStatus;
import rs.ac.uns.ftn.informatika.jpa.Enumerations.Seniority;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class RequestionResponseDTO {
    public Long id;
    public String positionInFirm;
    public String description;
    public String programmingLanguages;
    public Seniority seniority;
    public String location;
    public BigDecimal budget;
    public RequestionStatus status;
    public OffsetDateTime createdAt;
    public String name;
    public Long hiringId;
    public Long createdById;
    public String createdByFullName;
   // public String createdByFullName;
}
