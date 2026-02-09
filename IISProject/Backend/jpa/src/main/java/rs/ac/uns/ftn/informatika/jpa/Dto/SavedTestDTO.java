package rs.ac.uns.ftn.informatika.jpa.Dto;

public class SavedTestDTO {
    public String originalFileName;
    public String storedFileName;
    public String publicUrl;

    public SavedTestDTO(String originalFileName, String storedFileName, String publicUrl) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.publicUrl = publicUrl;
    }

}
