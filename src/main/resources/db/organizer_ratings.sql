-- Avaliação do organizador (1–5) por viagem, feita por cada participante após o fim da viagem.
CREATE TABLE IF NOT EXISTS organizer_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    viagem_id BIGINT NOT NULL,
    rater_user_id BIGINT NOT NULL,
    organizer_user_id BIGINT NOT NULL,
    estrelas TINYINT NOT NULL,
    testemunho TEXT NULL,
    createdAt DATETIME(6) NOT NULL,
    updatedAt DATETIME(6) NOT NULL,
    CONSTRAINT uq_organizer_rating_viagem_rater UNIQUE (viagem_id, rater_user_id),
    CONSTRAINT chk_organizer_rating_estrelas CHECK (estrelas >= 1 AND estrelas <= 5)
);

CREATE INDEX idx_organizer_ratings_organizer ON organizer_ratings (organizer_user_id);
