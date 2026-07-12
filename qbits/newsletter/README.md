# QBit Platform Newsletter

Spring Boot newsletter module implementing double opt-in, campaign/segment persistence, suppression compliance, and a transactional email outbox.

## Public API
- `POST /api/public/v1/newsletter/subscribe` — validates and normalizes an email, records pending consent, stores only a SHA-256 token hash, and queues confirmation mail.
- `GET /api/public/v1/newsletter/confirm?token=...` — consumes a valid 48-hour token once, confirms the subscription, and queues a CRM bridge event.
- `POST /api/public/v1/newsletter/unsubscribe` — marks the subscription unsubscribed and suppresses future delivery.

Apply per-IP/email gateway rate limits to the subscribe endpoint in the hosting application. The service also rejects currently suppressed addresses.

## Provider SPI
Implement `EmailProvider` for SendGrid, Mailgun, or SES. `SmtpEmailProvider` is selected by `NEWSLETTER_PROVIDER=smtp` and uses Spring Mail; SMTP host, port, AUTH, and STARTTLS are environment configurable in `application.yml`.

## Campaigns and CMS
`newsletter_campaign.cms_content_id` is the CMS bridge reference. `CampaignService.enqueue` materializes confirmed, non-suppressed recipients into the outbox. `newsletter_segment` supports dynamic criteria and static subscription ID sets; host applications may resolve criteria before enqueueing.

## Delivery
`OutboxWorker` polls due pending/retry records, checks suppression at send time, tracks provider IDs, and retries with capped exponential backoff before terminal failure. Bounce/webhook integrations add/update `email_suppression` records. `crm_sync` outbox messages provide the confirmed-subscriber CRM integration seam.

## Tests
Run `mvn test` from this directory (requires a working Maven installation/JAVA_HOME). H2 tests cover opt-in creation/confirmation/expiry/suppression and outbox delivery/retry/suppression.
