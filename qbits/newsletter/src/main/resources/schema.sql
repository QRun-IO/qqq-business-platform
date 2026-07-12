CREATE TABLE IF NOT EXISTS newsletter_subscription (
 id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL UNIQUE, status VARCHAR(20) NOT NULL CHECK (status IN ('pending','confirmed','unsubscribed')),
 preferences TEXT NOT NULL DEFAULT '{}', source_ip VARCHAR(64), confirmed_at TIMESTAMP WITH TIME ZONE, unsubscribed_at TIMESTAMP WITH TIME ZONE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE IF NOT EXISTS newsletter_segment (
 id UUID PRIMARY KEY, name VARCHAR(200) NOT NULL UNIQUE, segment_type VARCHAR(20) NOT NULL CHECK (segment_type IN ('dynamic','static')),
 criteria TEXT NOT NULL DEFAULT '{}', static_subscription_ids TEXT NOT NULL DEFAULT '[]', created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE IF NOT EXISTS newsletter_campaign (
 id UUID PRIMARY KEY, subject VARCHAR(500) NOT NULL, content TEXT NOT NULL, cms_content_id UUID, segment_id UUID REFERENCES newsletter_segment(id),
 targeting TEXT NOT NULL DEFAULT '{}', status VARCHAR(20) NOT NULL CHECK (status IN ('draft','scheduled','sending','sent','cancelled')),
 scheduled_at TIMESTAMP WITH TIME ZONE, sent_at TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE IF NOT EXISTS outbound_email (
 id UUID PRIMARY KEY, subscription_id UUID REFERENCES newsletter_subscription(id), campaign_id UUID REFERENCES newsletter_campaign(id),
 message_type VARCHAR(30) NOT NULL, recipient VARCHAR(320) NOT NULL, subject VARCHAR(500) NOT NULL, body TEXT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK (status IN ('pending','processing','sent','retry','failed','suppressed')),
 attempt_count INT NOT NULL DEFAULT 0, max_attempts INT NOT NULL DEFAULT 5, next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
 provider_message_id VARCHAR(255), last_error TEXT, created_at TIMESTAMP WITH TIME ZONE NOT NULL, sent_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS ix_outbound_due ON outbound_email(status,next_attempt_at);
CREATE TABLE IF NOT EXISTS email_suppression (
 id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL UNIQUE, reason VARCHAR(50) NOT NULL, source VARCHAR(100) NOT NULL,
 details TEXT, created_at TIMESTAMP WITH TIME ZONE NOT NULL, expires_at TIMESTAMP WITH TIME ZONE
);
CREATE TABLE IF NOT EXISTS opt_in_token (
 id UUID PRIMARY KEY, subscription_id UUID NOT NULL REFERENCES newsletter_subscription(id), token_hash VARCHAR(64) NOT NULL UNIQUE,
 expires_at TIMESTAMP WITH TIME ZONE NOT NULL, consumed_at TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_opt_in_hash ON opt_in_token(token_hash);
