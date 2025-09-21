package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
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
}
