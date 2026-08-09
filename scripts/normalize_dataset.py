import json
import os

INPUT_FILE = "data/raw/gita.json"
OUTPUT_FILE = "data/enriched/gita_normalized.json"

os.makedirs("data/enriched", exist_ok=True)

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    verses = json.load(f)

normalized = []

for verse in verses:

    prabhu = verse.get("prabhu") or {}

    normalized.append({
        "id": verse.get("_id"),
        "chapter": verse.get("chapter"),
        "verse": verse.get("verse"),

        "sanskrit": verse.get("slok", ""),

        "transliteration": verse.get("transliteration", ""),

        "translation": prabhu.get("et", ""),

        "commentary": prabhu.get("ec", ""),

        "summary": "",

        "keywords": [],

        "topics": [],

        "lifeSituations": [],

        "emotions": [],

        "questions": []
    })

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    json.dump(normalized, f, ensure_ascii=False, indent=2)

print(f"Normalized {len(normalized)} verses")
print(f"Saved to {OUTPUT_FILE}")