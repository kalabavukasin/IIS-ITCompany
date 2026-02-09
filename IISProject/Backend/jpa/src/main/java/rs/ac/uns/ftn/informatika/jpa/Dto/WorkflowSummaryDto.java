package rs.ac.uns.ftn.informatika.jpa.Dto;

import java.util.List;

public record WorkflowSummaryDto (
    Long id,
    String name,
    Integer version,
    Boolean active,
    List<String> stageNames
){}
