# Deploy — api_boraviajar (VPS)

## 1. Enviar código para o servidor

```bash
cd /opt/api_boraviajar
git pull
```

## 2. Build e restart da API

```bash
mvn -q -DskipTests package
sudo systemctl restart api-boraviajar
sudo systemctl status api-boraviajar --no-pager
```

Confirme versão:

```bash
curl -s https://api.boraviajar.net/api/v1/system/deploy-check
```

Esperado: `"securityRules": "2026-05-26-mobile-post-session-body-v1"` e `"mobilePostSessionBodyFallback": true`.

## 3. Migração MySQL (obrigatória para avaliação do organizador)

Se o log mostrar `Table 'boraviajar_db.organizer_ratings' doesn't exist`:

```bash
chmod +x /opt/api_boraviajar/deploy/run-migration.sh
bash /opt/api_boraviajar/deploy/run-migration.sh
```

Ou manualmente:

```bash
mysql -u root -p boraviajar_db < /opt/api_boraviajar/deploy/migrations/001_organizer_ratings.sql
```

Verificar:

```bash
mysql -u root -p boraviajar_db -e "DESCRIBE organizer_ratings;"
```

**Não é necessário** reiniciar a API após a migração.

## 4. Nginx (se POST autenticado ainda falhar)

Copie trechos de `deploy/nginx-api.boraviajar.conf.example` para o site ativo e garanta em todo `location`:

```nginx
proxy_set_header Authorization $http_authorization;
proxy_set_header X-App-Session $http_x_app_session;
proxy_set_header Cookie $http_cookie;
```

```bash
sudo nginx -t && sudo systemctl reload nginx
```

## 5. Teste rápido

```bash
curl -s "https://api.boraviajar.net/api/v1/health?timestamp=1"
```

No app mobile: salvar avaliação de viagem encerrada (participante confirmado).
