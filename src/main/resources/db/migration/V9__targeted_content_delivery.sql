-- Create Spring Modulith JPA Event Publication registry table
CREATE TABLE IF NOT EXISTS event_publication (
    id UUID NOT NULL,
    completion_attempts INTEGER NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    event_type VARCHAR(512) NOT NULL,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    listener_id VARCHAR(512) NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    serialized_event TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

-- Create index for event publication status querying
CREATE INDEX IF NOT EXISTS idx_event_publication_status ON event_publication (status);

-- Create notification schema
CREATE SCHEMA IF NOT EXISTS notification;

-- Create Web Push subscriptions table
CREATE TABLE IF NOT EXISTS notification.web_push_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    citizen_code VARCHAR(30) NOT NULL REFERENCES auth.users_auth(citizen_code) ON DELETE CASCADE,
    endpoint TEXT NOT NULL UNIQUE,
    p256dh VARCHAR(255) NOT NULL,
    auth VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Alter CMS content table to add targeting parameters
ALTER TABLE admins.cms_content 
    ADD COLUMN target_cohort_id INT REFERENCES cohort_placement.cohorts(id) ON DELETE SET NULL,
    ADD COLUMN target_state_residence auth.nigerian_state,
    ADD COLUMN target_state_origin auth.nigerian_state;

-- Create indexes for performance when filtering targeted content for users
CREATE INDEX IF NOT EXISTS idx_cms_content_target_cohort ON admins.cms_content (target_cohort_id);
CREATE INDEX IF NOT EXISTS idx_cms_content_target_state_res ON admins.cms_content (target_state_residence);
CREATE INDEX IF NOT EXISTS idx_cms_content_target_state_orig ON admins.cms_content (target_state_origin);
