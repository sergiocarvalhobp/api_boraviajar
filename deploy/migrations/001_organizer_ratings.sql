-- =============================================================================
-- Bora Viajar — migração 001: tabela organizer_ratings
-- Rode UMA vez no VPS:
--   bash /opt/api_boraviajar/deploy/run-migration.sh
-- ou:
--   mysql -u root -p boraviajar_db < deploy/migrations/001_organizer_ratings.sql
-- =============================================================================

USE boraviajar_db;

CREATE TABLE IF NOT EXISTS organizer_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    viagem_id BIGINT NOT NULL,
    rater_user_id BIGINT NOT NULL COMMENT 'Participante que avalia',
    organizer_user_id BIGINT NOT NULL COMMENT 'Líder da viagem',
    estrelas TINYINT NOT NULL COMMENT '1 a 5',
    testemunho TEXT NULL,
    createdAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updatedAt DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_organizer_rating_viagem_rater UNIQUE (viagem_id, rater_user_id),
    CONSTRAINT chk_organizer_rating_estrelas CHECK (estrelas >= 1 AND estrelas <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice (ignora erro 1061 se já existir)
SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'organizer_ratings'
      AND index_name = 'idx_organizer_ratings_organizer'
);
SET @ddl := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_organizer_ratings_organizer ON organizer_ratings (organizer_user_id)',
    'SELECT ''index já existe'' AS info'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'organizer_ratings' AS tabela, COUNT(*) AS linhas FROM organizer_ratings;
