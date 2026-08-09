import json
import os
import time
from pathlib import Path

from dotenv import load_dotenv
from google import genai
from tqdm import tqdm


# ==========================================
# CONFIG
# ==========================================

load_dotenv()

API_KEY = os.getenv("GEMINI_API_KEY")

if not API_KEY:
    raise Exception("GEMINI_API_KEY not found in .env")

INPUT_FILE = "data/enriched/gita_enriched.json"
OUTPUT_FILE = "data/enriched/gita_embeddings.json"
BACKUP_FILE = "data/enriched/gita_embeddings_backup.json"

MODEL = "gemini-embedding-001"

MAX_RETRIES = 5

client = genai.Client(api_key=API_KEY)


# ==========================================
# LOAD ORIGINAL DATASET
# ==========================================

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    original_dataset = json.load(f)

print(f"Original verses : {len(original_dataset)}")


# ==========================================
# RECOVER VALID VERSES FROM CORRUPTED JSON
# ==========================================

def recover_json_array(filepath):

    print("\n⚠️ Existing embedding file is corrupted.")
    print("Attempting automatic recovery...")

    with open(filepath, "r", encoding="utf-8") as f:
        text = f.read()

    decoder = json.JSONDecoder()

    recovered = []

    # Find beginning of JSON array
    start = text.find("[")

    if start == -1:
        print("No JSON array found.")
        return []

    position = start + 1

    while position < len(text):

        # Skip whitespace
        while position < len(text) and text[position].isspace():
            position += 1

        # Skip comma between objects
        if position < len(text) and text[position] == ",":
            position += 1

            while position < len(text) and text[position].isspace():
                position += 1

        # End of valid array
        if position < len(text) and text[position] == "]":
            break

        try:

            obj, next_position = decoder.raw_decode(
                text,
                position
            )

            if isinstance(obj, dict):
                recovered.append(obj)

            position = next_position

        except json.JSONDecodeError:

            print(
                f"Stopped recovery at character position {position}"
            )

            break

    print(
        f"Recovered {len(recovered)} complete verses "
        f"from corrupted file."
    )

    return recovered


# ==========================================
# LOAD / RECOVER EXISTING OUTPUT
# ==========================================

if Path(OUTPUT_FILE).exists():

    print("\nFound existing embedding file.")

    try:

        with open(OUTPUT_FILE, "r", encoding="utf-8") as f:
            dataset = json.load(f)

        print(
            f"Existing file loaded successfully: "
            f"{len(dataset)} verses"
        )

    except json.JSONDecodeError:

        recovered = recover_json_array(
            OUTPUT_FILE
        )

        if recovered:

            # Backup corrupted file
            try:
                import shutil

                shutil.copy2(
                    OUTPUT_FILE,
                    BACKUP_FILE
                )

                print(
                    f"Corrupted file backed up to:\n"
                    f"{BACKUP_FILE}"
                )

            except Exception as e:
                print(
                    f"Could not create backup: {e}"
                )

            dataset = recovered

            print(
                f"Continuing with {len(dataset)} "
                f"recovered verses."
            )

        else:

            print(
                "Could not recover any complete verses."
            )

            dataset = []

else:

    print(
        "\nNo previous embedding file found."
    )

    dataset = []


# ==========================================
# MERGE RECOVERED DATA WITH ORIGINAL DATASET
# ==========================================

existing_by_id = {
    verse["id"]: verse
    for verse in dataset
    if "id" in verse
}


merged_dataset = []

for original_verse in original_dataset:

    verse_id = original_verse["id"]

    if verse_id in existing_by_id:

        existing = existing_by_id[verse_id]

        # Keep original verse data
        # and preserve generated embedding
        merged = original_verse.copy()

        if "embedding" in existing:
            merged["embedding"] = existing["embedding"]

        merged_dataset.append(merged)

    else:

        merged_dataset.append(
            original_verse.copy()
        )


dataset = merged_dataset


# ==========================================
# FIND REMAINING VERSES
# ==========================================

remaining_indexes = []

for index, verse in enumerate(dataset):

    if not verse.get("embedding"):

        remaining_indexes.append(index)


print("\n========================================")
print("Embedding Status")
print("========================================")

print(
    f"Total verses      : {len(dataset)}"
)

print(
    f"Already embedded  : "
    f"{len(dataset) - len(remaining_indexes)}"
)

print(
    f"Remaining         : "
    f"{len(remaining_indexes)}"
)

print(
    f"Model             : {MODEL}"
)

print("========================================")


# ==========================================
# BUILD EMBEDDING TEXT
# ==========================================

def build_embedding_text(verse):

    summary = verse.get(
        "summary",
        ""
    )

    translation = verse.get(
        "translation",
        ""
    )

    keywords = ", ".join(
        verse.get(
            "keywords",
            []
        )
    )

    topics = ", ".join(
        verse.get(
            "topics",
            []
        )
    )

    return f"""
Bhagavad Gita Chapter {verse["chapter"]} Verse {verse["verse"]}

Summary:
{summary}

Translation:
{translation}

Keywords:
{keywords}

Topics:
{topics}
""".strip()


# ==========================================
# GENERATE EMBEDDING
# ==========================================

def generate_embedding(text):

    for attempt in range(MAX_RETRIES):

        try:

            response = client.models.embed_content(
                model=MODEL,
                contents=text
            )

            return response.embeddings[0].values

        except Exception as e:

            error = str(e)

            print(
                f"\nEmbedding attempt "
                f"{attempt + 1}/{MAX_RETRIES} failed:"
            )

            print(error)

            if "429" in error:

                print(
                    "Rate limit reached. "
                    "Waiting 60 seconds..."
                )

                time.sleep(60)

            elif "503" in error:

                print(
                    "Gemini service busy. "
                    "Waiting 20 seconds..."
                )

                time.sleep(20)

            else:

                time.sleep(10)

    return None


# ==========================================
# ATOMIC SAVE
# ==========================================

def save_dataset():

    temp_file = OUTPUT_FILE + ".tmp"

    with open(
        temp_file,
        "w",
        encoding="utf-8"
    ) as f:

        json.dump(
            dataset,
            f,
            ensure_ascii=False,
            indent=2
        )

        # Make sure everything is physically
        # written before replacing the real file
        f.flush()
        os.fsync(f.fileno())

    # Replace original only after successful write
    os.replace(
        temp_file,
        OUTPUT_FILE
    )


# ==========================================
# MAIN PROCESSING
# ==========================================

processed_this_run = 0

print("\nStarting embedding generation...\n")


for index in tqdm(
    remaining_indexes,
    desc="Generating embeddings"
):

    verse = dataset[index]

    verse_id = verse["id"]

    text = build_embedding_text(
        verse
    )

    embedding = generate_embedding(
        text
    )

    if embedding is None:

        print(
            f"\n❌ Failed permanently for {verse_id}"
        )

        continue

    # Add embedding
    verse["embedding"] = embedding

    # SAVE IMMEDIATELY
    # This means stopping the program loses
    # at most the current API call.
    save_dataset()

    processed_this_run += 1

    print(
        f"\nSaved embedding for "
        f"{verse_id} "
        f"({processed_this_run}/"
        f"{len(remaining_indexes)})"
    )


# ==========================================
# FINAL STATUS
# ==========================================

remaining = sum(
    1
    for verse in dataset
    if not verse.get("embedding")
)


print("\n========================================")
print("Embedding generation completed.")
print("========================================")

print(
    f"Generated this run : "
    f"{processed_this_run}"
)

print(
    f"Already completed  : "
    f"{len(dataset) - remaining}"
)

print(
    f"Remaining          : "
    f"{remaining}"
)

print(
    f"Output             : "
    f"{OUTPUT_FILE}"
)

if remaining == 0:

    print(
        "\n🎉 All 701 embeddings generated successfully!"
    )

else:

    print(
        "\nYou can safely run this script again."
    )

    print(
        "It will automatically resume."
    )