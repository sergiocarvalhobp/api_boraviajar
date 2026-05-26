#!/usr/bin/env bash
# Cria a tabela organizer_ratings no MySQL de produção.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SQL="$ROOT/deploy/migrations/001_organizer_ratings.sql"
ENV_FILE="${ENV_FILE:-/etc/api-boraviajar.env}"

if [[ ! -f "$SQL" ]]; then
  echo "Arquivo não encontrado: $SQL" >&2
  exit 1
fi

DB_NAME="boraviajar_db"
DB_USER="root"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE" 2>/dev/null || true
  if [[ -n "${DATABASE_URL:-}" ]]; then
  DB_NAME="$(echo "$DATABASE_URL" | sed -n 's|.*/\([^?]*\).*|\1|p')"
  fi
  DB_USER="${DB_USERNAME:-$DB_USER}"
fi

echo ">>> Aplicando migração em banco=$DB_NAME usuário=$DB_USER"
mysql -u "$DB_USER" -p "$DB_NAME" < "$SQL"
echo ">>> Migração concluída."
