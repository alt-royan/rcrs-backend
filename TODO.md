# TODO

## From Codebase

- [ ] **Refine CORS configuration** — `gateway-api/src/main/java/org/ultra/rcrs/gatewayapi/SecurityConfig.java:33`
  - `//TODO:доработать` — placeholder CORS config, needs to be properly configured.

## Найдено при разработке мобильного клиента

Обнаружено при проектировании `rcrs-frontend/music-app-mobile` (см. его `docs/PLAN.md`).
Порядок — от блокирующего к «знать заранее».

- [ ] **СЛОМАНО СЕЙЧАС: транскодирование падает при включённом loudnorm** — `services/media-service/src/main/java/org/ultra/rcrs/mediaservice/temporal/activity/impl/TranscodeAudioActivityImpl.java:51-55`
  - Строка фильтра собирается **вместе с флагом**:
    `String loudnorm = String.format("-af loudnorm=I=%s:LRA=%s:TP=%s", ...)`, а затем передаётся
    как значение того же флага: `"-af", loudnorm`. В итоге argv содержит
    `-af "-af loudnorm=I=-18:LRA=18:TP=-1"`, то есть имя фильтра — `-af loudnorm`.
  - Проверено запуском ffmpeg с ровно этой формой аргументов:
    `[AVFilterGraph] No such filter: '-af loudnorm'` → `Error opening output files: Filter not found`.
    Тот же вызов с `-af "loudnorm=..."` проходит успешно.
  - `loudnorm.enabled: true` — это **значение по умолчанию**
    (`media-service/src/main/resources/application.yaml:124-128`), то есть транскодирование не
    работает вообще, а не в краевом случае. Ветка `else` (без loudnorm) корректна.
  - Фикс: убрать `-af ` из формата строки — `String.format("loudnorm=I=%s:LRA=%s:TP=%s", ...)`.
  - Вероятно, это же и есть причина уже записанного ниже пункта «Не устанавливается duration»:
    без успешного транскода не появляются ни аудио-дорожки, ни их длительность.

- [ ] **Ошибки ffmpeg приходят пустыми — нечего читать в логах** — `TranscodeAudioActivityImpl.java:77` против `:36`
  - `pb.redirectError(ProcessBuilder.Redirect.DISCARD)` выбрасывает stderr, а обработчик ошибки
    на `:36` пытается собрать сообщение именно из него:
    `new String(process.getErrorStream().readAllBytes())`. Всегда пусто, поэтому любая ошибка
    выглядит как `FFmpeg failed (exit=1): ` без единой подробности — ровно то, что мешает
    заметить баг с loudnorm выше.
  - Фикс: `Redirect.PIPE` (и вычитывать поток, чтобы не заполнить буфер) либо
    перенаправлять stderr в файл/лог. Заодно не глотать stdout по той же причине.

- [ ] **Расширение выходного файла зашито как `.ogg`, хотя формат берётся из конфига** — `TranscodeAudioActivityImpl.java:26`
  - `File.createTempFile("audio-transcode-output-", ".ogg")` — литерал, тогда как контейнер
    задаётся `properties.getFormat()` и передаётся в `-f` (`:62`, `:74`).
  - Сменить `format: ogg` на что-либо другое в конфиге недостаточно: ffmpeg по явному `-f`
    запишет нужный контейнер, но файл будет называться `.ogg`. Если контейнер где-то ниже
    определяется по имени файла, в БД попадёт `ogg`, и `StreamingService.contentType()`
    отдаст `audio/ogg` для MP4 — плеер получит неверный тип.
  - Фикс: строить суффикс из `properties.getFormat()`. Сделать **до** перехода на AAC, иначе
    смена конфига даст молча неправильный content-type.

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

- [ ] **Транскод по умолчанию Ogg/Vorbis — iOS его не играет** — `services/media-service/src/main/resources/application.yaml:120-123`
  - `codec: libvorbis`, `format: ogg`. В AVFoundation нет декодера Vorbis, поэтому AVPlayer не
    сыграет `.ogg` — при корректном `audio/ogg` и работающих Range-запросах. Дело не в
    транспорте: побайтовая отдача из S3 в порядке, отсутствует именно декодер. На Android
    (ExoPlayer/Media3) Vorbis декодируется штатно, то есть баг будет выглядеть как «на Android
    играет, на iPhone нет». Смена библиотеки плеера не поможет: и `expo-audio`, и
    `react-native-track-player` на iOS используют тот же AVPlayer.
  - Для справки: Spotify раздаёт Ogg/Vorbis, но у них свой декодер в кроссплатформенном ядре и
    свой транспорт — PCM отдаётся напрямую в AudioUnit, AVPlayer не участвует. Этот путь
    означает собственный нативный плеер и потерю системной интеграции (локскрин, Control
    Center, AirPlay, CarPlay, обработка прерываний) плюс софтовое декодирование вместо
    аппаратного. Несопоставимо по цене с правкой конфига.
  - **Фикс: AAC-LC в контейнере MP4/M4A.** Аппаратное декодирование на обеих платформах,
    качество на бит выше MP3, поддерживается gapless. Конкретно:
    - `application.yaml:121` `codec: libvorbis` → `codec: aac`
    - `application.yaml:123` `format: ogg` → `format: m4a`
    - `application.yaml:120` `bitrates: 128k, 192k, 320k` → `96k, 128k, 192k` (для AAC 320k —
      выброшенный трафик, прозрачность достигается раньше). Публичный контракт менять не
      обязательно: имена `LOW/MID/HIGH` можно перепривязать в `StreamingService.java:33-37`.
    - `StreamingService.java:72-78` — добавить `case "m4a", "mp4" -> "audio/mp4"`, иначе
      отдастся `application/octet-stream`.
    - **Добавить `-movflags +faststart`** в вызов ffmpeg (`TranscodeAudioActivityImpl.java:52-75`).
      Без него `moov`-атом остаётся в конце файла, и плеер вынужден сначала дочитать хвост:
      на presigned-ссылке это лишний round-trip до начала звука, а часть плееров на таком
      просто не стартует. Для Ogg такой проблемы не было, поэтому флага в коде нет — проверено.
    - `validation.formats: mp3, wav, ogg, flac` не трогать: это разрешённые **входные** форматы.
  - Не подходят: **Opus** — AVPlayer не берёт его из Ogg (нужен CAF или fMP4, поддержка
    неровная); **MP3** — играет везде, но хуже качество на бит и нет gapless, разумен только
    как дополнительный профиль совместимости.
  - Сделать до заливки каталога. Позже это батч-перетранскодирование оригиналов из
    `rcrs-upload` — не катастрофа, но лишняя работа.
  - Отдельно на будущее: `/media/stream` отдаёт один progressive-объект, HLS/DASH нет —
    адаптивного битрейта и переключения качества на ходу не будет. Прогрессивный AAC этап
    закрывает; HLS добавляется позже без смены контракта `/media/stream`.

- [ ] **`signature-duration: 6h` для стриминга слишком долго** — `services/media-service/src/main/resources/application.yaml:114`
  - Presigned-ссылка — это обычный публичный URL без авторизации: кто её получил, тот шесть
    часов может скачивать и пересылать трек, не имея токена. Для стримингового сервиса это
    фактически раздача контента наружу.
  - Разумный срок — минуты (5–15): его хватает начать воспроизведение и досидеть до конца
    трека, а клиент и так перезапрашивает `/media/stream` при ошибке, потому что presign
    истекает по определению.
  - Учесть при выборе: ссылка должна переживать длинный трек целиком, иначе воспроизведение
    оборвётся на середине; либо срок с запасом от максимальной длительности
    (`validation.duration.max: 2h`), либо клиент обновляет ссылку по ошибке 403.
  - У `download` уже `1h` (`:145`), у `upload` — `24h`: сроки стоит осознанно развести по
    назначению, а не оставлять как получилось.

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
