package com.qrunio.platform.newsletter;
public interface EmailProvider {
 DeliveryResult send(EmailMessage message) throws Exception;
 record EmailMessage(String to,String subject,String body){}
 record DeliveryResult(String providerMessageId){}
}
