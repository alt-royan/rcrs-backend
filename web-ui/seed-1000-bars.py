#!/usr/bin/env python3
import json
import os
import urllib.request
import urllib.error
from pathlib import Path
from urllib.parse import urlparse, urlunparse, urlencode

UPLOAD_API = "http://localhost:8082"
CATALOG_API = "http://localhost:8090"
DIR = Path(__file__).resolve().parent
AUDIO_DIR = DIR / "1000 bars"

KEYCLOAK_URL = "http://laptop-smgs968r:8180/realms/master/protocol/openid-connect/token"
KEYCLOAK_CLIENT_ID = "rcrs-app"
KEYCLOAK_CLIENT_SECRET = "BkQy6CR09D3RrgIyvs4qg5yuoshkM7xv"
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


def load_json(name):
    path = DIR / name
    log(f"Loading {path}")
    return json.loads(path.read_text(encoding="utf-8"))


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


def wait_for_complete(uids, token, poll_interval=3):
    import time
    attempt = 0
    while True:
        attempt += 1
        log(f"Poll #{attempt}: {len(uids)} uids")
        resp = post_json(UPLOAD_API, f"/upload/audio/status?uids={'&uids='.join(uids)}", {}, token=token)
        statuses = {s["uid"]: s["status"] for s in resp}
        log(f"  Statuses: {statuses}")
        if all(s in ("COMPLETE", "FAILED") for s in statuses.values()):
            failed = [uid for uid, st in statuses.items() if st == "FAILED"]
            if failed:
                log(f"  WARNING: failed uids: {failed}")
            return resp
        time.sleep(poll_interval)


log("Starting seed script")
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

album = load_json("1000 bars.json")
seed_uids(album, uid_map)

log("\n=== Step 2: Upload audio files ===")
upload_files(presign_map)

log("\n=== Step 3: Create artist ===")
artist_resp = post_json(
    CATALOG_API,
    "/artists",
    load_json("heronwater.json"),
    token=token
)
artist_id = artist_resp["id"]
log(f"Artist ID: {artist_id}")

log("\n=== Step 4: Patch album JSON with artist ID ===")
for a in album["artists"]:
    a["id"] = artist_id
for track in album["tracks"]:
    for a in track["artists"]:
        a["id"] = artist_id

log("\n=== Step 5: Create album ===")
album_resp = post_json(
    CATALOG_API,
    "/albums",
    album,
    token=token
)
album_id = album_resp["id"]
log(f"Album ID: {album_id}")

log("\n=== Step 6: Poll audio processing status ===")
all_uids = list(uid_map.values())
final_statuses = wait_for_complete(all_uids, token)
for s in final_statuses:
    log(f"  Final: uid={s['uid']} status={s['status']} reason={s.get('reason','')}")

log(f"\nDone. Album ID: {album_id}")
