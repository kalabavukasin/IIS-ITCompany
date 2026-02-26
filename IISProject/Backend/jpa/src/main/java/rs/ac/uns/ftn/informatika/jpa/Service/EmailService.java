package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailsender) {
        this.mailSender = mailsender;
    }

    public void sendActivationEmail(String to, String token) {
        String activationUrl = "http://localhost:4200/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Account Activation");
        message.setText("Please click the following link to activate your account: " + activationUrl);

        mailSender.send(message);
    }

    public void sendWeeklySummary(String to, String summary) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Weekly Summary");
        message.setText(summary);

        mailSender.send(message);
    }

    public void sendApplicationRefusedNotification(String to, String candidateName, String jobTitle, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Application Update – " + jobTitle);
        message.setText(
            "Dear " + candidateName + ",\n\n" +
            "Thank you for applying for the position of " + jobTitle + ".\n" +
            "After careful consideration, we regret to inform you that your application has not been successful at this time.\n\n" +
            (reason != null && !reason.isBlank() ? "Reason: " + reason + "\n\n" : "") +
            "We wish you all the best in your job search.\n\n" +
            "Best regards,\nThe Recruitment Team"
        );
        mailSender.send(message);
    }

    public void sendTestRefusedWithScoreNotification(String to, String candidateName, String jobTitle,
                                                     BigDecimal score, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Test Result – " + jobTitle);
        message.setText(
            "Dear " + candidateName + ",\n\n" +
            "Thank you for completing the test for the position of " + jobTitle + ".\n" +
            "Your test score: " + score + " / 100\n\n" +
            "Unfortunately, your score did not meet the required threshold and your application will not be moving forward.\n\n" +
            (reason != null && !reason.isBlank() ? "Additional note: " + reason + "\n\n" : "") +
            "We appreciate the time you invested and wish you success in your future endeavours.\n\n" +
            "Best regards,\nThe Recruitment Team"
        );
        mailSender.send(message);
    }

    public void sendTestInviteNotification(String to, String candidateName, String jobTitle, String deadline) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Test Invitation – " + jobTitle);
        message.setText(
            "Dear " + candidateName + ",\n\n" +
            "Congratulations! You have been invited to complete an online test as part of the selection process for " + jobTitle + ".\n\n" +
            "Please log in to the recruitment portal to access and submit your test before: " + deadline + "\n\n" +
            "Good luck!\n\n" +
            "Best regards,\nThe Recruitment Team"
        );
        mailSender.send(message);
    }

    public void sendInterviewScheduledNotification(String to, String candidateName, String jobTitle,
                                                   String interviewType, String scheduledAt, String location) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Interview Invitation – " + jobTitle);
        message.setText(
            "Dear " + candidateName + ",\n\n" +
            "We are pleased to invite you to an interview for the position of " + jobTitle + ".\n\n" +
            "Details:\n" +
            "  Type:      " + formatInterviewType(interviewType) + "\n" +
            "  Date/Time: " + scheduledAt + "\n" +
            "  Location:  " + location + "\n\n" +
            "Best regards,\nThe Recruitment Team"
        );
        mailSender.send(message);
    }

    public void sendOfferNotification(String to, String candidateName, String jobTitle,
                                      String startDate, String validUntil) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Job Offer – " + jobTitle);
        message.setText(
            "Dear " + candidateName + ",\n\n" +
            "We are thrilled to offer you the position of " + jobTitle + "!\n\n" +
            "Offer details:\n" +
            "  Start Date:  " + startDate + "\n" +
            "  Valid Until: " + validUntil + "\n\n" +
            "Please log in to the recruitment portal to accept or decline this offer before the expiry date.\n\n" +
            "We look forward to welcoming you to the team!\n\n" +
            "Best regards,\nThe Recruitment Team"
        );
        mailSender.send(message);
    }

    private String formatInterviewType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "HR_SCREEN"     -> "HR Screen";
            case "TECHNICAL"     -> "Technical Interview";
            case "SYSTEM_DESIGN" -> "System Design Interview";
            case "MANAGERIAL"    -> "Managerial Interview";
            case "FINAL"         -> "Final Interview";
            default              -> type;
        };
    }
}
