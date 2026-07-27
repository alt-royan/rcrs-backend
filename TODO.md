# TODO

## From Codebase

- [ ] **Refine CORS configuration** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:33`
  - `//TODO:доработать` — placeholder CORS config, needs to be properly configured.

## Найдено при разработке мобильного клиента

Обнаружено при проектировании `rcrs-frontend/music-app-mobile` (см. его `docs/PLAN.md`).
Порядок — от блокирующего к «знать заранее».

- [ ] **БЕЗОПАСНОСТЬ: плейлисты не проверяют владельца** — `services/playlist-service/src/main/java/org/ultra/rcrs/playlistservice/service/PlaylistService.java:66-133`
  - `ownerId` пишется только при создании (`:40`), а дальше `findById` (`:78`, `:108`) и
    `deleteById` (`:131`) идут без всякого предиката по `ownerId`. Любой авторизованный пользователь
    может прочитать, изменить и **удалить чужой плейлист**, зная только его id.
  - Флаг `isPrivate` тоже игнорируется, в том числе в `getPlaylistsByIds`.
  - Фикс: сверять `ownerId` с `jwt.getSubject()` во всех операциях, кроме чтения публичных;
    для чтения — отдавать 404 на приватные чужие.

- [ ] **Нет эндпоинта «мои плейлисты»** — `services/playlist-service/.../controller/PlaylistController.java:33-38`
  - Есть только `GET /playlists?ids=a,b,c`, то есть клиент обязан заранее знать id. Списка
    своих плейлистов получить нельзя ничем.
  - Из-за этого экран «Библиотека» в мобильном приложении невозможен и исключён из MVP.
  - Фикс: `GET /playlists/mine` (owner из JWT, с пагинацией и фильтром по `type`). Типы
    `LIKED | HISTORY | DOWNLOADS | CUSTOM` уже есть в `model/PlaylistType.java` — то есть
    лайки и история задуманы, но недостижимы.

- [ ] **Нет батч-эндпоинта для треков → N+1 на экране плейлиста**
  - `GET /playlists/{id}/tracks` отдаёт только `trackId/position/addedAt`
    (`dto/response/PlaylistTrackViewDto.java:14-18`), без метаданных, а в каталоге есть только
    `GET /api/catalog/tracks/{id}` по одному. Плейлист на 50 треков — 51 запрос с телефона.
  - Фикс: `GET /api/catalog/tracks?ids=...` в metadata-read-service (или отдавать метаданные
    сразу в ответе playlist-service).

- [ ] **Анонимного чтения нет вообще** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:73`
  - `anyExchange().authenticated()`: токен требуется даже на публичные `GET /api/catalog/**`
    и `GET /api/search`. То есть «витрину» нельзя показать до логина, нельзя расшарить ссылку
    на альбом, и первый же экран с данными зависит от Keycloak.
  - Решить продуктово: если витрина должна быть открытой — разрешить анонимно `publ`-пути
    каталога и поиск.

- [ ] **CORS разрешает только `localhost:3000`** — `gateway-api/.../SecurityConfig.java:38`
  - Нативному мобильному приложению безразлично, но Expo-web и любой другой origin
    (прод-домен админки, Storybook) получат отказ. Пересекается с уже открытым пунктом
    «Refine CORS configuration» выше — закрывать вместе.

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
