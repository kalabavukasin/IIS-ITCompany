package rs.ac.uns.ftn.informatika.jpa.Mapper;

import rs.ac.uns.ftn.informatika.jpa.Dto.CreateRequestionDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.RequestionResponseDTO;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;

public class RequestionMapper {
    public static Requestion toEntity(CreateRequestionDTO dto) {
        Requestion r = new Requestion();
        r.setPositionTitle(dto.positionInFirm);
        r.setDescription(dto.description);
        r.setSkills(dto.programmingLanguages);
        r.setSeniority(dto.seniority);
        r.setLocation(dto.location);
        r.setBudget(dto.budget);
        r.setName(dto.name);
        return r;
    }
    public static RequestionResponseDTO toDto(Requestion r) {
        RequestionResponseDTO d = new RequestionResponseDTO();
        d.id = r.getId();
        d.positionInFirm = r.getPositionTitle();
        d.description = r.getDescription();
        d.programmingLanguages = r.getSkills();
        d.seniority = r.getSeniority();
        d.location = r.getLocation();
        d.budget = r.getBudget();
        d.status = r.getStatus();
        d.createdAt = r.getCreatedAt();
        d.name = r.getName();
        d.hiringId = r.getHiringManager().getId();
        /*d.createdByFullName = r.getCreatedBy() != null
                ? r.getCreatedBy().getFirstName() + " " + r.getCreatedBy().getLastName()
                : null;*/
        d.createdById = (r.getCreatedBy() != null) ? r.getCreatedBy().getId() : null;
        return d;
    }
}
