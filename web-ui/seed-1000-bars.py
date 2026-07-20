#!/usr/bin/env python3
import json
import urllib.request
from pathlib import Path

API = "http://localhost:8082"
DIR = Path(__file__).resolve().parent


def post_json(path, data):
    req = urllib.request.Request(
        f"{API}{path}",
        data=json.dumps(data).encode(),
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read())


def load_json(name):
    return json.loads((DIR / name).read_text(encoding="utf-8"))


print("1. Creating artist...")
artist_resp = post_json("/artists", load_json("heronwater.json"))
artist_id = artist_resp["id"]
print(f"   Artist ID: {artist_id}")

print("2. Patching album JSON with artist ID...")
album = load_json("1000 bars.json")
for a in album["artists"]:
    a["id"] = artist_id
for track in album["tracks"]:
    for a in track["artists"]:
        a["id"] = artist_id

print("3. Creating album...")
album_resp = post_json("/albums", album)
album_id = album_resp["id"]
print(f"   Album ID: {album_id}")
print(f"\nDone. Album ID: {album_id}")
