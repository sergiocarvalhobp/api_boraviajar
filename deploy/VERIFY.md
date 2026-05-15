# Verificar API em produção

Após deploy, estes endpoints devem responder assim em `https://api.boraviajar.net`:

```bash
# Público — deve ser 200
curl -s "https://api.boraviajar.net/api/v1/health"

# Público após deploy recente — deve ser 401 (token inválido), não 403
curl -s -o /dev/null -w "%{http_code}" -X POST "https://api.boraviajar.net/api/v1/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"access_token":"test"}'
```

| Código | Significado |
|--------|-------------|
| **403** | Versão antiga: Spring Security bloqueia a rota. Faça redeploy do JAR atual. |
| **401** | Rota pública OK; corpo `test` não é token Auth0 válido (esperado). |
| **503** | `AUTH0_DOMAIN` ou `JWT_SECRET` ausente em `/etc/api-boraviajar.env`. |

Rebuild e restart no servidor:

```bash
cd /opt/api_boraviajar
mvn -q -DskipTests package
sudo systemctl restart api-boraviajar
```
