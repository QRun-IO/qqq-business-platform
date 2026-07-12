package com.qrunio.platform.newsletter;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration public class NewsletterConfiguration {
 @Bean Clock newsletterClock(){return Clock.systemUTC();}
 @Bean String confirmationUrl(@Value("${newsletter.confirmation-base-url}") String value){return value;}
}
