# Migrações MySQL

| Arquivo | Descrição |
|---------|-----------|
| `001_organizer_ratings.sql` | Tabela de avaliação 1–5 do organizador + testemunho |

## Executar no VPS

```bash
bash /opt/api_boraviajar/deploy/run-migration.sh
```

Credenciais: lê `DB_USERNAME` e banco de `DATABASE_URL` em `/etc/api-boraviajar.env` quando existir.
