-- Rode se a tabela organizer_ratings já existir sem a coluna testemunho.
ALTER TABLE organizer_ratings
    ADD COLUMN testemunho TEXT NULL AFTER estrelas;
