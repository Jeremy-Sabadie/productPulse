-- US03/US08/US14 : conservation des avis existants.
-- Les anciens retours n'avaient pas de catégorie : valeur de reprise technique.
ALTER TABLE feedback
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'IMPROVEMENT';
