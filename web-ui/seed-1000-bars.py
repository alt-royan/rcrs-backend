#!/usr/bin/env python3
import json
import os
import urllib.request
import urllib.error
from pathlib import Path

API = "http://localhost:8082"
DIR = Path(__file__).resolve().parent
AUDIO_DIR = DIR / "1000 bars"


def log(msg):
    print(f"[SEED] {msg}")


def post_json(path, data):
    url = f"{API}{path}"
    body = json.dumps(data).encode()
    log(f"POST {url} body={data}")
    req = urllib.request.Request(
        url,
        data=body,
        headers={"Content-Type": "application/json"},
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
    data = file_path.read_bytes()
    log(f"PUT {url} size={len(data)}")
    req = urllib.request.Request(url, data=data, method="PUT")
    for h in headers:
        req.add_header(h["key"], h["value"])
    try:
        with urllib.request.urlopen(req) as resp:
            log(f"  -> {resp.status}")
            return resp.status
    except urllib.error.HTTPError as e:
        log(f"  -> ERROR {e.code} {e.read().decode()}")
        raise


def load_json(name):
    path = DIR / name
    log(f"Loading {path}")
    return json.loads(path.read_text(encoding="utf-8"))


def get_presign(file_path):
    size = os.path.getsize(file_path)
    log(f"Pre-sign: {file_path.name} ({size} bytes)")
    return post_json("/upload/audio/pre-sign", {
        "name": file_path.name,
        "length": size,
    })


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
            put_file(presign["url"], presign["headers"], audio_file)
            ok += 1
        except Exception as e:
            log(f"  FAILED upload {title}: {e}")
            fail += 1
    log(f"Upload summary: {ok} ok, {fail} failed")


def wait_for_complete(uids, poll_interval=3):
    import time
    attempt = 0
    while True:
        attempt += 1
        log(f"Poll #{attempt}: {len(uids)} uids")
        resp = post_json(f"/upload/audio/status?uids={'&uids='.join(uids)}", {})
        statuses = {s["uid"]: s["status"] for s in resp}
        log(f"  Statuses: {statuses}")
        if all(s in ("COMPLETE", "FAILED") for s in statuses.values()):
            failed = [uid for uid, st in statuses.items() if st == "FAILED"]
            if failed:
                log(f"  WARNING: failed uids: {failed}")
            return resp
        time.sleep(poll_interval)


log("Starting seed script")
log(f"API={API}")
log(f"AUDIO_DIR={AUDIO_DIR}")

log("\n=== Step 1: Pre-sign audio files ===")
audio_files = sorted(AUDIO_DIR.glob("*.mp3"))
log(f"Found {len(audio_files)} mp3 files")
uid_map = {}
presign_map = {}

for audio_file in audio_files:
    title = audio_file.stem
    presign = get_presign(audio_file)
    uid = presign["uid"]
    uid_map[title] = uid
    presign_map[audio_file] = presign

log(f"Pre-sign complete: {len(uid_map)} uid mappings")

album = load_json("1000 bars.json")
seed_uids(album, uid_map)

log("\n=== Step 2: Upload audio files ===")
upload_files(presign_map)

log("\n=== Step 3: Create artist ===")
artist_resp = post_json("/artists", load_json("heronwater.json"))
artist_id = artist_resp["id"]
log(f"Artist ID: {artist_id}")

log("\n=== Step 4: Patch album JSON with artist ID ===")
for a in album["artists"]:
    a["id"] = artist_id
for track in album["tracks"]:
    for a in track["artists"]:
        a["id"] = artist_id

log("\n=== Step 5: Create album ===")
album_resp = post_json("/albums", album)
album_id = album_resp["id"]
log(f"Album ID: {album_id}")

log("\n=== Step 6: Poll audio processing status ===")
all_uids = list(uid_map.values())
final_statuses = wait_for_complete(all_uids)
for s in final_statuses:
    log(f"  Final: uid={s['uid']} status={s['status']} reason={s.get('reason','')}")

log(f"\nDone. Album ID: {album_id}")
