-- ProductPulse
-- V4 : votes des utilisateurs sur les feedbacks approuvés.

CREATE TABLE feedback_vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    feedback_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_feedback_vote
        PRIMARY KEY (id),

    CONSTRAINT uk_feedback_vote_feedback_user
        UNIQUE (feedback_id, user_id),

    CONSTRAINT fk_feedback_vote_feedback
        FOREIGN KEY (feedback_id)
        REFERENCES feedback(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_feedback_vote_user
    ON feedback_vote(user_id);
