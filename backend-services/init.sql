-- =============================================================================
-- ShieldAI Platform — Database Schema
-- Version: 2.0 (Phase 3 — Enhanced for Production)
-- =============================================================================

-- ============================================
-- 1. USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN', 'MODERATOR')),
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    locale VARCHAR(10) DEFAULT 'en',
    avatar_url VARCHAR(500),
    notification_preferences JSONB DEFAULT '{"push": true, "email": true, "sms": false}'::jsonb,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);

-- ============================================
-- 2. REFRESH TOKENS TABLE (JWT Token Rotation)
-- ============================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    device_info VARCHAR(255)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- ============================================
-- 3. DEVICES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS devices (
    device_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    device_token VARCHAR(500), -- FCM/APNs push token
    platform VARCHAR(20) NOT NULL CHECK (platform IN ('ANDROID', 'IOS', 'WEB')),
    os_version VARCHAR(30),
    app_version VARCHAR(20),
    model_name VARCHAR(100),
    model_version VARCHAR(50), -- AI model version on device
    is_active BOOLEAN DEFAULT TRUE,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_devices_user ON devices(user_id);
CREATE INDEX idx_devices_platform ON devices(platform);

-- ============================================
-- 4. SCAN LOGS TABLE (Core Threat Detection)
-- ============================================
CREATE TABLE IF NOT EXISTS scan_logs (
    scan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    channel VARCHAR(30) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'URL', 'QR', 'CALL', 'NOTIFICATION', 'AD', 'MANUAL')),
    content_hash VARCHAR(255),
    content_snippet VARCHAR(500), -- First 500 chars for display (never full PII)
    sender_info VARCHAR(255),
    risk_score INT NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    risk_label VARCHAR(20) NOT NULL CHECK (risk_label IN ('SAFE', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    ai_model_version VARCHAR(50),
    ai_confidence DECIMAL(5,4), -- 0.0000 to 1.0000
    ai_explanation TEXT,
    is_false_positive BOOLEAN DEFAULT FALSE,
    user_feedback VARCHAR(20), -- CORRECT, FALSE_POSITIVE, FALSE_NEGATIVE
    scanned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scan_logs_user ON scan_logs(user_id);
CREATE INDEX idx_scan_logs_channel ON scan_logs(channel);
CREATE INDEX idx_scan_logs_risk ON scan_logs(risk_label);
CREATE INDEX idx_scan_logs_date ON scan_logs(scanned_at DESC);
CREATE INDEX idx_scan_logs_content_hash ON scan_logs(content_hash);

-- ============================================
-- 5. THREAT HISTORY TABLE (Aggregated Timeline)
-- ============================================
CREATE TABLE IF NOT EXISTS threat_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    source_channel VARCHAR(30) NOT NULL,
    scan_id UUID REFERENCES scan_logs(scan_id),
    risk_score INT NOT NULL,
    risk_label VARCHAR(20) NOT NULL,
    summary VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_threat_history_user ON threat_history(user_id);
CREATE INDEX idx_threat_history_date ON threat_history(timestamp DESC);
CREATE INDEX idx_threat_history_risk ON threat_history(risk_label);

-- ============================================
-- 6. THREAT REPORTS TABLE (Community Crowdsource)
-- ============================================
CREATE TABLE IF NOT EXISTS threat_reports (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id),
    category VARCHAR(50) NOT NULL CHECK (category IN ('PHISHING', 'SCAM', 'SPAM', 'MALWARE', 'IMPERSONATION', 'OTHER')),
    channel VARCHAR(30),
    content_reference TEXT,
    sender_info VARCHAR(255),
    description TEXT,
    evidence_urls TEXT[], -- Array of screenshot/evidence URLs
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWING', 'CONFIRMED', 'REJECTED', 'RESOLVED')),
    upvote_count INT DEFAULT 0,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES users(user_id)
);

CREATE INDEX idx_threat_reports_user ON threat_reports(user_id);
CREATE INDEX idx_threat_reports_status ON threat_reports(status);
CREATE INDEX idx_threat_reports_category ON threat_reports(category);
CREATE INDEX idx_threat_reports_date ON threat_reports(submitted_at DESC);

-- ============================================
-- 7. BLOCKED SENDERS TABLE (User Blocklist)
-- ============================================
CREATE TABLE IF NOT EXISTS blocked_senders (
    block_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    sender_identifier VARCHAR(255) NOT NULL, -- Phone number, email, or domain
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('PHONE', 'EMAIL', 'DOMAIN', 'IP')),
    reason VARCHAR(255),
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_blocked_senders_user ON blocked_senders(user_id);
CREATE INDEX idx_blocked_senders_identifier ON blocked_senders(sender_identifier);
CREATE UNIQUE INDEX idx_blocked_senders_unique ON blocked_senders(user_id, sender_identifier);

-- ============================================
-- 8. NOTIFICATION LOG TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS notification_log (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    type VARCHAR(30) NOT NULL CHECK (type IN ('THREAT_ALERT', 'SCAN_RESULT', 'REPORT_UPDATE', 'SYSTEM', 'PROMOTION')),
    is_read BOOLEAN DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notification_log(user_id);
CREATE INDEX idx_notifications_read ON notification_log(is_read);
CREATE INDEX idx_notifications_date ON notification_log(created_at DESC);
