
-- ProductPulse
-- V2 : rattachement des feedbacks à une application et un auteur.
--
-- Les colonnes sont ajoutées comme obligatoires.
-- Cette migration suppose que la table feedback est encore vide.

ALTER TABLE feedback
    ADD COLUMN application_code VARCHAR(100) NOT NULL,
    ADD COLUMN author_id BIGINT NOT NULL;
