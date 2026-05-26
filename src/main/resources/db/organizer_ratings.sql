-- Avaliação do organizador (1–5) por viagem, feita por cada participante após o fim da viagem.
-- Produção: preferir deploy/migrations/001_organizer_ratings.sql (idempotente).

USE boraviajar_db;

CREATE TABLE IF NOT EXISTS organizer_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    viagem_id BIGINT NOT NULL,
    rater_user_id BIGINT NOT NULL,
    organizer_user_id BIGINT NOT NULL,
    estrelas TINYINT NOT NULL,
    testemunho TEXT NULL,
    createdAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_organizer_rating_viagem_rater UNIQUE (viagem_id, rater_user_id),
    CONSTRAINT chk_organizer_rating_estrelas CHECK (estrelas >= 1 AND estrelas <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
