ALTER TABLE feedback
    ADD COLUMN github_issue_number BIGINT NULL,
    ADD COLUMN github_issue_url VARCHAR(500) NULL;

ALTER TABLE feedback
    ADD CONSTRAINT uk_feedback_github_issue_number
    UNIQUE (github_issue_number);
