
-- ProductPulse
-- V1 : création de la table des feedbacks.
-- Structure initiale du module Feedback.

CREATE TABLE feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_feedback PRIMARY KEY (id)
);
