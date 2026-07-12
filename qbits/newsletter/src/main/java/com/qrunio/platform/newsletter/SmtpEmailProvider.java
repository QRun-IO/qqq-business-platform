package com.qrunio.platform.newsletter;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="newsletter.provider",havingValue="smtp",matchIfMissing=true)
public class SmtpEmailProvider implements EmailProvider {
 private final JavaMailSender sender; private final String from;
 public SmtpEmailProvider(JavaMailSender sender,@Value("${newsletter.from}") String from){this.sender=sender;this.from=from;}
 public DeliveryResult send(EmailMessage m){SimpleMailMessage mail=new SimpleMailMessage();mail.setFrom(from);mail.setTo(m.to());mail.setSubject(m.subject());mail.setText(m.body());sender.send(mail);return new DeliveryResult("smtp-"+UUID.randomUUID());}
}
