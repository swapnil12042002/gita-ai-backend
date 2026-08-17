import json
import os
from pathlib import Path

import psycopg2
from dotenv import load_dotenv


# ==========================================
# CONFIG
# ==========================================

load_dotenv()

INPUT_FILE = "data/enriched/gita_embeddings.json"

DB_HOST = os.getenv("POSTGRES_HOST", "localhost")
DB_PORT = os.getenv("POSTGRES_PORT", "5432")
DB_NAME = os.getenv("POSTGRES_DB", "gita_ai")
DB_USER = os.getenv("POSTGRES_USER", "postgres")
DB_PASSWORD = os.getenv("POSTGRES_PASSWORD")

TABLE_NAME = "gita_verses"

EMBEDDING_DIMENSION = 3072


# ==========================================
# CHECK CONFIG
# ==========================================

if not DB_PASSWORD:
    raise Exception("POSTGRES_PASSWORD not found in .env")

if not Path(INPUT_FILE).exists():
    raise FileNotFoundError(
        f"Embedding file not found: {INPUT_FILE}"
    )


# ==========================================
# LOAD JSON
# ==========================================

print("Loading embedding dataset...")

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    verses = json.load(f)

print(f"Total verses found: {len(verses)}")


# ==========================================
# CONNECT TO POSTGRES
# ==========================================

print("Connecting to PostgreSQL...")

connection = psycopg2.connect(
    host=DB_HOST,
    port=DB_PORT,
    database=DB_NAME,
    user=DB_USER,
    password=DB_PASSWORD,
)

cursor = connection.cursor()

print("Connected successfully.")


# ==========================================
# INSERT / UPDATE
# ==========================================

query = f"""
INSERT INTO {TABLE_NAME} (
    id,
    chapter,
    verse,
    sanskrit,
    transliteration,
    translation,
    commentary,
    summary,
    keywords,
    topics,
    life_situations,
    emotions,
    questions,
    embedding
)
VALUES (
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s,
    %s::vector
)
ON CONFLICT (id)
DO UPDATE SET
    chapter = EXCLUDED.chapter,
    verse = EXCLUDED.verse,
    sanskrit = EXCLUDED.sanskrit,
    transliteration = EXCLUDED.transliteration,
    translation = EXCLUDED.translation,
    commentary = EXCLUDED.commentary,
    summary = EXCLUDED.summary,
    keywords = EXCLUDED.keywords,
    topics = EXCLUDED.topics,
    life_situations = EXCLUDED.life_situations,
    emotions = EXCLUDED.emotions,
    questions = EXCLUDED.questions,
    embedding = EXCLUDED.embedding;
"""


# ==========================================
# PROCESS
# ==========================================

processed = 0
skipped = 0

print("\nStarting ingestion...\n")

try:

    for verse in verses:

        verse_id = verse.get("id")
        embedding = verse.get("embedding")

        if not embedding:
            print(f"Skipping {verse_id} - embedding missing")
            skipped += 1
            continue

        if len(embedding) != EMBEDDING_DIMENSION:
            print(
                f"Skipping {verse_id} - "
                f"expected {EMBEDDING_DIMENSION} dimensions, "
                f"got {len(embedding)}"
            )
            skipped += 1
            continue

        cursor.execute(
            query,
            (
                verse["id"],
                verse["chapter"],
                verse["verse"],
                verse["sanskrit"],
                verse.get("transliteration"),
                verse.get("translation"),
                verse.get("commentary"),
                verse.get("summary"),
                verse.get("keywords", []),
                verse.get("topics", []),
                verse.get("lifeSituations", []),
                verse.get("emotions", []),
                verse.get("questions", []),
                "[" + ",".join(map(str, embedding)) + "]",
            ),
        )

        processed += 1

        if processed % 50 == 0:
            connection.commit()
            print(f"Processed: {processed}/{len(verses)}")


    connection.commit()

    print("\n========================================")
    print("Embedding ingestion completed.")
    print("========================================")
    print(f"Processed : {processed}")
    print(f"Skipped   : {skipped}")
    print(f"Table     : {TABLE_NAME}")


except Exception as e:

    connection.rollback()

    print("\n❌ Ingestion failed.")
    print(e)

    raise


finally:

    cursor.close()
    connection.close()

    print("PostgreSQL connection closed.")