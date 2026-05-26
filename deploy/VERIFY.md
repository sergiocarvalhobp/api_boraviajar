# Verificar API em produção

## 1. O JAR novo está rodando?

```bash
curl -s "https://api.boraviajar.net/api/v1/system/deploy-check"
```

Resposta esperada (JSON):

```json
{"securityRules":"2026-05-15-mobile-auth-v2","mobileAuthTokenPublic":true,...}
```

| Resultado | O que fazer |
|-----------|-------------|
| **404** ou JSON sem `securityRules` | JAR antigo ainda em execução — veja passo 2 |
| **200** com `mobileAuthTokenPublic: true` | Spring OK — se login ainda falha, veja passo 3 (nginx) |

## 2. Rebuild e restart (no VPS)

```bash
cd /opt/api_boraviajar
git pull   # ou copie o código atualizado
mvn -q -DskipTests package
sudo systemctl restart api-boraviajar
sudo systemctl status api-boraviajar
```

Confirme que o `ExecStart` do systemd aponta para o JAR que acabou de gerar:

`target/api-boraviajar-0.1.0-SNAPSHOT.jar`

## 3. Testes HTTP

```bash
curl -s "https://api.boraviajar.net/api/v1/health"
curl -s "https://api.boraviajar.net/api/v1/system/deploy-check"
curl -s -o /dev/null -w "auth_token:%{http_code}\n" -X POST "https://api.boraviajar.net/api/v1/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"access_token":"test"}'

curl -s -o /dev/null -w "public_session:%{http_code}\n" -X POST "https://api.boraviajar.net/api/v1/public/session" \
  -H "Content-Type: application/json" \
  -d '{"access_token":"test"}'
```

| `POST /auth/token` | Significado |
|--------------------|-------------|
| **403** e `deploy-check` **200** com v2 | **nginx** bloqueando POST — use `deploy/nginx-api.boraviajar.conf.example` |
| **403** e `deploy-check` **404** | JAR antigo — repetir passo 2 |
| **401** com JSON `error` | Rota pública OK (token `test` inválido, esperado) |
| **503** | Falta `AUTH0_DOMAIN` ou `JWT_SECRET` em `/etc/api-boraviajar.env` |

## 4. POST com sessão (avaliação / join)

Se `GET /trips/{id}` mostra `myStatus` mas `POST .../avaliar-organizador` retorna **401**:

1. Confirme JAR com `mobilePostSessionBodyFallback: true` em `deploy-check`
2. Atualize nginx (`deploy/nginx-api.boraviajar.conf.example`) — repasse `Authorization`, `X-App-Session` e `Cookie`
3. App mobile envia também `sessionToken` no JSON e `app_session_id` na query (fallback)
