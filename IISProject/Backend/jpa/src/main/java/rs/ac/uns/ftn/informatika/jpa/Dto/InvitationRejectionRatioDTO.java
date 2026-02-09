package rs.ac.uns.ftn.informatika.jpa.Dto;

public class InvitationRejectionRatioDTO {
    private Long totalInvited;
    private Long totalRejected;
    private Double rejectionRate;
    
    public InvitationRejectionRatioDTO() {}
    
    public InvitationRejectionRatioDTO(Long totalInvited, Long totalRejected, Double rejectionRate) {
        this.totalInvited = totalInvited;
        this.totalRejected = totalRejected;
        this.rejectionRate = rejectionRate;
    }
    
    public Long getTotalInvited() {
        return totalInvited;
    }
    
    public void setTotalInvited(Long totalInvited) {
        this.totalInvited = totalInvited;
    }
    
    public Long getTotalRejected() {
        return totalRejected;
    }
    
    public void setTotalRejected(Long totalRejected) {
        this.totalRejected = totalRejected;
    }
    
    public Double getRejectionRate() {
        return rejectionRate;
    }
    
    public void setRejectionRate(Double rejectionRate) {
        this.rejectionRate = rejectionRate;
    }
}
