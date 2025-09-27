package rs.ac.uns.ftn.informatika.jpa.Dto;

public class StaffMemberDTO {
    public Long id;
    public String fullName;
    public String role;

    public StaffMemberDTO() {}

    public StaffMemberDTO(Long id, String fullName, String role) {
        this.id = id;
        this.fullName = fullName;
        this.role = role;
    }
}
