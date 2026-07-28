# TODO

## From Codebase

## Найдено при разработке мобильного клиента

Обнаружено при проектировании `rcrs-frontend/music-app-mobile` (см. его `docs/PLAN.md`).
Порядок — от блокирующего к «знать заранее».

- [ ] **Анонимного чтения нет вообще** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:73`
  - `anyExchange().authenticated()`: токен требуется даже на публичные `GET /api/catalog/**`
    и `GET /api/search`. То есть «витрину» нельзя показать до логина, нельзя расшарить ссылку
    на альбом, и первый же экран с данными зависит от Keycloak.
  - Решить продуктово: если витрина должна быть открытой — разрешить анонимно `publ`-пути
    каталога и поиск.

- [ ] **Realm и клиенты Keycloak не в version control** — `local/infra/keycloak/`
  - В папке только `Dockerfile`; Keycloak запускается `start-dev` с admin/admin и
    persistent-волюмом (`local/infra/docker-compose.yml:253-281`), realm-export отсутствует.
    Все клиенты, роли и маппер `rcrs-roles` заведены руками в консоли и невоспроизводимы:
    новый разработчик или чистая машина поднять окружение не сможет.
  - Плюс приложение живёт во встроенном realm **`master`**, а не в своём — админский realm
    для прикладных пользователей использовать не стоит.
  - Фикс: закоммитить realm-export JSON и подключить `--import-realm`; заодно завести
    public-клиент для мобильного (PKCE S256, redirect `musicapp://redirect`) декларативно.
  - Примечание: `music-app-mobile/.env.example` ссылается на realm `music` и порты
    8081/8082/8083 — не соответствует реальности (`master`, шлюз `:8099`, Keycloak `:8180`).
