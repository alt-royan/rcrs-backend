# TODO

## From Codebase

- [ ] **Refine CORS configuration** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:33`
  - `//TODO:доработать` — placeholder CORS config, needs to be properly configured.

- [ ] **`GET /me` всегда отдаёт 404** — `services/user-service/src/main/java/org/ultra/rcrs/userservice/service/UserService.java:105`
  - `getCompactProfile` получает из контроллера `jwt.getSubject()` (`controller/UserController.java:23`),
    но ищет пользователя через `findByUsername(userId)` — то есть сравнивает `sub` с `username`.
    Соседний `getProfile` (`:88`) делает верно: `findByUserId`.
  - Фикс: заменить `findByUsername` на `findByUserId` в `getCompactProfile`.
  - До фикса клиентам использовать только `GET /me/profile`.

- [ ] **`POST /media/stream` должен быть `GET`** — `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/controller/StreamingController.java:20`
  - Эндпоинт ничего не меняет: принимает два query-параметра (`trackId`, `quality`), тела нет,
    возвращает presigned-ссылку. POST для чистого чтения ломает семантику HTTP, запрещает
    кэширование и мешает клиентам (мобильные плееры и CDN ожидают GET).
  - Фикс: `@PostMapping` → `@GetMapping`. Рядом `DownloadController` уже использует GET —
    привести к одному виду.
  - Поменять синхронно на клиентах; в `config/SecurityConfig.java:42` есть ещё мёртвый матчер
    `/track/*/stream` на путь, которого не существует — заодно убрать.

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

- [ ] **В публичном каталоге нет пагинации** — `services/metadata-read-service/.../controller/publ/*`
  - `GET /artists/{id}/albums` и `GET /albums/{id}/tracks` возвращают bare JSON array: без
    обёртки, без `total`, без `offset/limit` — вся дискография и весь трек-лист целиком.
    Пагинация есть только в search-service.
  - Для мобильного это трафик и память на популярных артистах.
  - Фикс: `offset/limit` + та же обёртка `PaginationResponse`, что используется в `/admin/*`.

- [ ] **Обложки недостижимы с устройства и хосты рассинхронизированы**
  - `cdn.images.endpoint` задан по-разному: **только** metadata-read использует достижимый
    хост `http://192.168.1.3:4566/images` (`metadata-read-service/application.yaml:21`), а
    остальные четыре — нерезолвящийся `http://images.localhost:4566`
    (`metadata-write-service/application.yaml:38`, `playlist-service/application.yaml:38`,
    `search-service/application.yaml:23`, `media-service/application.yaml:132`).
    Поэтому один и тот же альбом приходит с рабочей картинкой из каталога и с битой из
    поиска — что ровно совпадает с уже записанным ниже пунктом «Нет картинок при поисках».
  - `S3Utils.parseUrl` возвращает `null` при пустом ключе (`shared-lib/.../utils/S3Utils.java:27-32`),
    так что **все** `coverUrl`/`avatarUrl` nullable — это нормально, но должно быть в контракте.
  - Фикс: один внешне достижимый CDN-хост во всех сервисах (одна переменная окружения на все
    пять, а не пять независимых значений в пяти `application.yaml`).

- [ ] **DTO должны отдавать URL всех размеров изображения, а не один оригинал**
  - Сейчас в DTO лежит единственный `coverUrl`/`avatarUrl` — результат
    `S3Utils.parseUrl(imageKey)` (`shared-lib/src/main/java/org/ultra/rcrs/utils/S3Utils.java:27-32`),
    то есть ссылка на **оригинал**. Ссылок на превью API не отдаёт вообще, поэтому клиент
    обязан сам склеивать `{coverUrl}/64x64`, `/300x300`, `/640x640` — ключи с такими
    суффиксами создаются при загрузке в
    `media-service/src/main/java/org/ultra/rcrs/mediaservice/service/ImageUploadService.java:28-31`.
  - Почему это плохо:
    - Схема ключей превью — внутренняя деталь хранилища, а её вынуждены знать все клиенты
      (мобильный, админка, будущий веб). Меняется схема — ломаются все сразу.
    - Набор размеров **берётся из конфига** (`imageProperties.getThumbnails().getSizes()`,
      сейчас `image.thumbnails.sizes: 64,300,640` в `media-service/application.yaml:133-134`,
      в dev — из `IMAGE_THUMBNAILS_SIZES`). Клиент хардкодит три значения и тихо отдаёт 404
      на картинках, если конфиг изменят. Про это нельзя узнать из API.
    - Клиент не может отличить «превью ещё не сгенерировано» от «размера не существует» —
      в обоих случаях просто битая ссылка.
    - Отдавая только оригинал, API провоцирует тянуть полноразмерную картинку в строку списка.
  - Фикс: заменить строковое поле на объект с готовыми ссылками, например
    `cover: { original, sm, md, lg }` (или `{ "64": url, "300": url, "640": url }`, если хочется
    оставить связь с размерами явной), и собирать его на сервере из того же конфига, что
    генерирует превью. Затрагивает `AlbumPublicViewDto`, `AlbumPublicStandaloneDto`,
    `ArtistPublicViewDto`, `TrackPublicViewDto`, `TrackPublicStandaloneDto`, search-результаты,
    `PlaylistViewDto`, `UserProfileResponse` — то есть выносить в общий хелпер в `shared-lib`
    рядом с `S3Utils`, а не дублировать по сервисам.
  - Поле остаётся nullable (`parseUrl` возвращает `null` на пустом ключе) — это нормально, но
    должно быть в контракте, чтобы клиенты обязательно рисовали плейсхолдер.
  - До этого фикса в мобильном приложении вся логика живёт в одной точке —
    `music-app-mobile/src/utils/images.ts` (подмена хоста, суффикс размера, плейсхолдер).
    После фикса этот файл сводится к плейсхолдеру и удаляется почти целиком.

- [ ] **Привести все даты и метки времени к единому типу (`Instant` / `LocalDate`)**
  - Сейчас в кодовой базе два несовместимых подхода к одному и тому же смыслу: `user-service`
    хранит `createdAt`/`updatedAt` как `Instant` (`user-service/.../model/User.java:34,37`), а
    `playlist-service` те же поля — как `LocalDateTime`
    (`playlist-service/.../model/Playlist.java:64,67`). Всего `LocalDateTime` встречается в
    **23** файлах, `Instant` — в **14**.
  - Проблема на проводе: `LocalDateTime` сериализуется **без зоны и без offset**
    (`2024-03-01T00:00:00`). Клиент не может узнать, в какой зоне это записано, и любой парсер,
    трактующий такую строку как UTC, сдвинет время — у пользователя в другой зоне даты
    «поедут». Затронуты `PlaylistViewDto` (`createdAt`, `updatedAt`), `PlaylistTrackViewDto`
    (`addedAt`), публичные и админские DTO альбома и трека (`releaseDate`).
  - **Метки времени** (`createdAt`, `updatedAt`, `addedAt`) → `Instant`, на проводе ISO-8601 в
    UTC с `Z`. Это то, чем они и являются — момент во времени.
  - **`releaseDate` → `LocalDate`, а не `Instant`.** Дата релиза — календарная дата, времени у
    неё нет (в базе всегда `T00:00:00`, см. `metadata-write-service/.../model/Album.java:38`).
    Если сделать её инстантом, появится фиктивное время суток и зона: `2024-03-01T00:00:00Z`
    в зоне UTC−5 отрисуется как **29 февраля**, то есть смена типа сама создаст сдвиг дат,
    который мы и пытаемся убрать. `LocalDate` даёт на проводе `"2024-03-01"` — без зоны, без
    двусмысленности, без сдвига.
  - Заодно: в DTO альбома рядом с `releaseDate` есть отдельное поле `year`, дублирующее из неё
    год (и в search-результатах оно `String`, а в каталоге `Integer` — ещё и типы разъехались).
    После перехода на `LocalDate` год считается на клиенте, поле можно убрать из контракта.
  - Менять синхронно: JPA/Mongo-модели, DTO, мапперы, protobuf-события в
    `shared-lib/src/main/proto/events` и Liquibase-миграции (`timestamp` →
    `timestamp with time zone`), иначе тип разъедется между write- и read-стороной CQRS.

- [ ] **Настроить OpenAPI-документацию, сейчас она полностью автогенерируется**
  - `springdoc` подключён и шлюз агрегирует спеки пяти сервисов
    (`gateway-api/src/main/resources/application.yaml:69-77`), но в коде **ноль** аннотаций
    `@Operation`, `@ApiResponse`, `@Schema`, `@Tag` — проверено по всем сервисам. Спека
    выводится только рефлексией, то есть содержит имена полей и типы и **больше ничего**.
  - Чего в ней из-за этого нет:
    - Описаний эндпоинтов и полей — читающий не понимает разницы между `GET /me` и
      `GET /me/profile`, между `/stream` и `/download`, между `admin`- и `publ`-вариантами.
    - Кодов ошибок: 400/401/403/404 не описаны нигде, хотя сервисы их возвращают
      (`NotFoundException`, `BadRequestException`).
    - Ограничений и семантики параметров: что `q` и `type` в поиске обязательны, что `type` —
      в нижнем регистре, что `sort` принимает только `asc`/`desc` и падает 500 на всём
      остальном, что у `size` нет верхней границы, что `quality` — это `LOW|MID|HIGH`.
    - Nullability: все `coverUrl`/`avatarUrl` могут быть `null`, но в спеке это не отражено.
    - Формы ответов, которые не выводятся из типа: `GET /media/audios` отдаёт
      `Map<UUID, AudioItemGroupBy>` — в спеке это невнятный `object`.
    - Примеров запросов/ответов — ни одного.
  - Почему это стоит закрыть: спека — единственный контракт между бэкендом и клиентами
    (экспорта схемы нигде больше нет), и типы мобильного приложения планируется генерировать
    из `/v3/api-docs`. Всё, чего в спеке нет, превращается в догадки на стороне клиента и в
    расхождения, которые всплывают только в рантайме.
  - Фикс: `@Tag` на контроллеры, `@Operation(summary/description)` на методы,
    `@ApiResponse` на реальные коды ошибок, `@Schema(description, nullable, example)` на поля
    DTO и enum-ы; общий `OpenAPI`-бин с названием, версией и `securityScheme` bearer-JWT
    (сейчас в Swagger UI нельзя авторизоваться, а без токена не работает ни один запрос).
    Начать с публичных путей — их потребляет мобильное приложение.

- [ ] **Транскод по умолчанию Ogg/Vorbis — iOS его не играет** — `services/media-service/src/main/resources/application.yaml:120-123`
  - `codec: libvorbis`, `format: ogg`. AVPlayer нативно Vorbis не декодирует, значит
    воспроизведение на iPhone не заработает вообще, а не «заработает хуже».
  - Фикс: профиль AAC (`.m4a`) или MP3. Сделать до заливки каталога — иначе потребуется
    перетранскодирование всей библиотеки.
  - Отдельно на будущее: `/media/stream` отдаёт один progressive-объект, HLS/DASH нет —
    адаптивного битрейта и нормального переключения качества на ходу не будет.

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

## Additional

- [ ] Fix tests and test all functions
- [ ] Tests in read service
- [ ] Tests in playlist service
- [ ] Tests in search service
- 
- [ ] Не устанавливается duration
- Лагает фронт
- Нет картинок при поисках
- нужен даунлод ендпоит
