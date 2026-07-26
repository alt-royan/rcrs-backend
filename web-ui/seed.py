#!/usr/bin/env python3
import base64
import json
import os
import urllib.request
import urllib.error
from pathlib import Path
from urllib.parse import urlencode

UPLOAD_API = "http://localhost:8099/media"
CATALOG_API = "http://localhost:8099/workflow"
DIR = Path(__file__).resolve().parent

# ---- Edit these two consts to seed a different release ----
ARTIST_FILES = [
    DIR / "PHARAOH.json",
    DIR / "The Chemodan.json",
    DIR / "LIL MORTY.json",
    DIR / "39.json",
    DIR / "Acid Drop King.json",
    DIR / "Boulevard Depo.json",
    DIR / "Mnogoznaal.json",
    DIR / "noa.json",
]
ALBUM_FILE = DIR / "pink-phloyd.json"
AUDIO_DIR = DIR / "Pink Phloyd"
# -------------------------------------------------------------

IMAGES_DIR = DIR / "images"
IMAGE_MIME_TYPES = {".jpg": "image/jpeg", ".jpeg": "image/jpeg", ".png": "image/png"}

KEYCLOAK_URL = "http://192.168.1.3:8180/realms/master/protocol/openid-connect/token"
KEYCLOAK_CLIENT_ID = "rcrs-app"
KEYCLOAK_CLIENT_SECRET = "8mYnsi1nVXg4Oa5usCRmgEn6EIEniMP7"
KEYCLOAK_USERNAME = "bog"
KEYCLOAK_PASSWORD = "bog"


def log(msg):
    print(f"[SEED] {msg}")


def get_keycloak_token():
    log(f"Authenticating with Keycloak: {KEYCLOAK_URL}")
    data = urlencode({
        "client_id": KEYCLOAK_CLIENT_ID,
        "client_secret": KEYCLOAK_CLIENT_SECRET,
        "username": KEYCLOAK_USERNAME,
        "password": KEYCLOAK_PASSWORD,
        "grant_type": "password",
    }).encode()

    req = urllib.request.Request(
        KEYCLOAK_URL,
        data=data,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )

    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read())
        token = result["access_token"]
        log(f"  -> token acquired (expires in {result.get('expires_in', '?')}s)")
        return token


def normalize_presign_url(url):
    return url.replace("http://s3:4566", "http://localhost:4566")


def post_json(base_url, path, data, token=None):
    url = f"{base_url}{path}"
    body = json.dumps(data).encode()

    log(f"POST {url} body={data}")

    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(
        url,
        data=body,
        headers=headers,
        method="POST",
    )

    try:
        with urllib.request.urlopen(req) as resp:
            result = json.loads(resp.read())
            log(f"  -> {resp.status} {result}")
            return result

    except urllib.error.HTTPError as e:
        log(f"  -> ERROR {e.code} {e.read().decode()}")
        raise


def put_file(url, headers, file_path):
    size = file_path.stat().st_size

    log(f"PUT {url} size={size}")

    req = urllib.request.Request(
        url,
        method="PUT",
    )

    # ВАЖНО:
    # presigned URL подписывает эти headers,
    # поэтому передаем их как есть
    for h in headers:
        for key, value in h.items():
            req.add_header(key, value)

    req.add_header("Content-Length", str(size))

    try:
        with open(file_path, "rb") as f:
            with urllib.request.urlopen(req, data=f) as resp:
                log(f"  -> {resp.status}")
                return resp.status

    except urllib.error.HTTPError as e:
        log(f"  -> ERROR {e.code} {e.read().decode()}")
        raise


def load_json(path):
    log(f"Loading {path}")
    return json.loads(Path(path).read_text(encoding="utf-8"))


def get_presign(file_path, token):
    size = os.path.getsize(file_path)
    log(f"Pre-sign: {file_path.name} ({size} bytes)")
    return post_json(UPLOAD_API, "/upload/audio/pre-sign", {
        "name": file_path.name,
        "length": size,
    }, token=token)


def seed_uids(album, uid_map):
    matched = 0
    skipped = []
    for track in album["tracks"]:
        title = track["title"]
        if title in uid_map:
            track["uid"] = uid_map[title]
            matched += 1
            log(f"  uid seeded: {title} -> {uid_map[title]}")
        else:
            skipped.append(title)
    log(f"  Total: {matched} seeded, {len(skipped)} skipped")
    if skipped:
        log(f"  Skipped: {skipped}")


def upload_files(presign_map):
    ok = 0
    fail = 0
    for audio_file, presign in presign_map.items():
        title = audio_file.stem
        try:
            upload_url = normalize_presign_url(presign["url"])

            put_file(
                upload_url,
                presign["headers"],
                audio_file
            )
            ok += 1
        except Exception as e:
            log(f"  FAILED upload {title}: {e}")
            fail += 1
    log(f"Upload summary: {ok} ok, {fail} failed")


def find_image(name):
    for ext in IMAGE_MIME_TYPES:
        candidate = IMAGES_DIR / f"{name}{ext}"
        if candidate.exists():
            return candidate
    return None


def upload_image(image_path, token):
    mime = IMAGE_MIME_TYPES[image_path.suffix.lower()]
    encoded = base64.b64encode(image_path.read_bytes()).decode()
    data_url = f"data:{mime};base64,{encoded}"
    log(f"Uploading image: {image_path.name}")
    resp = post_json(UPLOAD_API, "/upload/image", {"image": data_url}, token=token)
    return resp["uri"]


def create_artists(artist_files, token):
    """Create every artist in ARTIST_FILES, return {lowercased name: id}."""
    name_to_id = {}
    for artist_file in artist_files:
        artist = load_json(artist_file)

        image_path = find_image(artist["name"])
        if image_path:
            artist["avatarUri"] = upload_image(image_path, token)
            log(f"  Avatar set for {artist['name']}: {image_path.name}")
        else:
            log(f"  No avatar image found for {artist['name']}")

        resp = post_json(CATALOG_API, "/artists", artist, token=token)
        name_to_id[artist["name"].lower()] = resp["id"]
        log(f"  Artist created: {artist['name']} -> {resp['id']}")
    return name_to_id


def resolve_artist_refs(refs, name_to_id):
    for ref in refs:
        name = ref.get("name", "")
        artist_id = name_to_id.get(name.lower())
        if artist_id is None:
            log(f"  WARNING: no created artist matches '{name}'")
            continue
        ref["id"] = artist_id


def patch_album_artists(album, name_to_id):
    resolve_artist_refs(album.get("artists", []), name_to_id)
    for track in album["tracks"]:
        resolve_artist_refs(track.get("artists", []), name_to_id)
        resolve_artist_refs(track.get("others", []), name_to_id)


log("Starting seed script")
log(f"ALBUM_FILE={ALBUM_FILE}")
log(f"AUDIO_DIR={AUDIO_DIR}")

token = get_keycloak_token()

log("\n=== Step 1: Pre-sign audio files ===")
audio_files = sorted(AUDIO_DIR.glob("*.mp3"))
log(f"Found {len(audio_files)} mp3 files")
uid_map = {}
presign_map = {}

for audio_file in audio_files:
    title = audio_file.stem
    presign = get_presign(audio_file, token)
    presign["url"] = normalize_presign_url(presign["url"])
    uid = presign["uid"]
    uid_map[title] = uid
    presign_map[audio_file] = presign

log(f"Pre-sign complete: {len(uid_map)} uid mappings")

album = load_json(ALBUM_FILE)
seed_uids(album, uid_map)

log("\n=== Step 2: Upload audio files ===")
upload_files(presign_map)

log("\n=== Step 3: Create artists ===")
artist_name_to_id = create_artists(ARTIST_FILES, token)
log(f"Artists created: {artist_name_to_id}")

log("\n=== Step 4: Patch album JSON with artist IDs ===")
patch_album_artists(album, artist_name_to_id)

log("\n=== Step 5: Upload album cover ===")
cover_path = find_image(album["title"])
if cover_path:
    album["coverUri"] = upload_image(cover_path, token)
    log(f"  Cover set: {cover_path.name}")
else:
    log(f"  No cover image found for {album['title']}")

log("\n=== Step 6: Create album ===")
album_resp = post_json(
    CATALOG_API,
    "/albums",
    album,
    token=token
)
album_id = album_resp["id"]
log(f"Album ID: {album_id}")

log(f"\nDone. Album ID: {album_id}")
