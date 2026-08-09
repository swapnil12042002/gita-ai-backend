import json
import os
import time
from pathlib import Path

from dotenv import load_dotenv
from google import genai
from google.genai import types
from tqdm import tqdm

# ==========================================
# CONFIG
# ==========================================

load_dotenv()

API_KEY = os.getenv("GEMINI_API_KEY")

if not API_KEY:
    raise Exception("GEMINI_API_KEY not found in .env")

INPUT_FILE = "data/enriched/gita_normalized.json"
OUTPUT_FILE = "data/enriched/gita_enriched.json"

MODEL = "gemini-3.5-flash-lite"

BATCH_SIZE = 10
MAX_RETRIES = 5

client = genai.Client(api_key=API_KEY)

# ==========================================
# LOAD DATASET
# ==========================================

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    original_dataset = json.load(f)

# ==========================================
# RESUME SUPPORT
# ==========================================

if Path(OUTPUT_FILE).exists():

    print("Found existing enriched dataset. Resuming...")

    with open(OUTPUT_FILE, "r", encoding="utf-8") as f:
        dataset = json.load(f)

else:

    print("No previous output found. Starting fresh...")

    dataset = original_dataset

# ==========================================
# FIND REMAINING VERSES
# ==========================================

remaining_indexes = []

for index, verse in enumerate(dataset):

    if not verse.get("summary"):
        remaining_indexes.append(index)

print(f"Total verses           : {len(dataset)}")
print(f"Remaining verses       : {len(remaining_indexes)}")
print(f"Batch size             : {BATCH_SIZE}")
print(f"Total batches          : {(len(remaining_indexes)+BATCH_SIZE-1)//BATCH_SIZE}")

# ==========================================
# SAVE FUNCTION
# ==========================================

def save_dataset():

    with open(
        OUTPUT_FILE,
        "w",
        encoding="utf-8"
    ) as f:

        json.dump(
            dataset,
            f,
            ensure_ascii=False,
            indent=2
        )
# ==========================================
# PROMPT
# ==========================================

SYSTEM_PROMPT = """
You are an expert in the Bhagavad Gita.

You will receive multiple verses.

Return ONLY a valid JSON array.

One object for EACH verse.

Each object MUST follow this schema:

{
  "id": "",
  "summary": "",
  "keywords": [],
  "topics": [],
  "lifeSituations": [],
  "emotions": [],
  "questions": []
}

Rules:

- Return every verse.
- Never skip a verse.
- Summary must be 2-3 concise sentences.
- Keywords: 5-10 items.
- Topics: 2-5 items.
- Life situations: practical real-world situations.
- Emotions: emotions addressed by the verse.
- Questions: natural user questions the verse answers.

Return ONLY JSON.

No markdown.

No explanation.

No ```json block.
"""

# ==========================================
# BUILD PROMPT
# ==========================================

def build_prompt(batch):

    prompt = SYSTEM_PROMPT

    prompt += "\n\nVerses:\n"

    for verse in batch:

        prompt += f"""

ID: {verse["id"]}

Chapter: {verse["chapter"]}

Verse: {verse["verse"]}

Sanskrit:
{verse["sanskrit"]}

Translation:
{verse["translation"]}

-------------------------------------

"""

    return prompt

# ==========================================
# GEMINI API
# ==========================================

def enrich_batch(batch):

    prompt = build_prompt(batch)

    for attempt in range(MAX_RETRIES):

        try:

            response = client.models.generate_content(

                model=MODEL,

                contents=prompt,

                config=types.GenerateContentConfig(

                    temperature=0.2,

                    response_mime_type="application/json"

                )

            )

            text = response.text.strip()

            text = text.replace("```json", "")
            text = text.replace("```", "")
            text = text.strip()

            if not text:
                raise Exception("Empty response received from Gemini.")

            parsed = json.loads(text)

            if not isinstance(parsed, list):
                raise Exception("Gemini did not return a JSON array.")

            return parsed

        except Exception as e:

            error = str(e)

            print(f"\nAttempt {attempt+1}/{MAX_RETRIES}")

            print(error)

            if "429" in error:

                print("Rate limit reached. Waiting 60 seconds...")

                time.sleep(60)

            elif "503" in error:

                print("Model overloaded. Waiting 20 seconds...")

                time.sleep(20)

            elif "Expecting" in error:

                print("Invalid JSON. Waiting 5 seconds...")

                time.sleep(5)

            else:

                time.sleep(10)

    return None

# ==========================================
# MAIN BATCH PROCESSING
# ==========================================

processed_this_run = 0

total_batches = (
    len(remaining_indexes) + BATCH_SIZE - 1
) // BATCH_SIZE

print("\nStarting enrichment...\n")

for batch_number in tqdm(range(total_batches), desc="Processing Batches"):

    start = batch_number * BATCH_SIZE
    end = start + BATCH_SIZE

    batch_indexes = remaining_indexes[start:end]

    if not batch_indexes:
        break

    batch = [dataset[i] for i in batch_indexes]

    print(
        f"\nProcessing Batch "
        f"{batch_number + 1}/{total_batches}"
    )

    result = enrich_batch(batch)

    if result is None:

        print(
            "Batch failed after maximum retries."
        )

        continue

    # ==========================================
    # MAP RESPONSE BY ID
    # ==========================================

    response_map = {}

    for item in result:

        if "id" not in item:

            print(
                "Warning: Response missing id."
            )

            continue

        response_map[item["id"]] = item

    # ==========================================
    # MERGE INTO DATASET
    # ==========================================

    updated_count = 0

    for index in batch_indexes:

        verse = dataset[index]

        verse_id = verse["id"]

        if verse_id not in response_map:

            print(
                f"Missing response for {verse_id}"
            )

            continue

        metadata = response_map[verse_id]

        verse["summary"] = metadata.get(
            "summary",
            ""
        )

        verse["keywords"] = metadata.get(
            "keywords",
            []
        )

        verse["topics"] = metadata.get(
            "topics",
            []
        )

        verse["lifeSituations"] = metadata.get(
            "lifeSituations",
            []
        )

        verse["emotions"] = metadata.get(
            "emotions",
            []
        )

        verse["questions"] = metadata.get(
            "questions",
            []
        )

        updated_count += 1
        processed_this_run += 1

    print(
        f"Updated {updated_count} verses."
    )

    # ==========================================
    # SAVE AFTER EVERY BATCH
    # ==========================================

    save_dataset()

    print(
        f"Saved dataset. "
        f"Processed this run: {processed_this_run}"
    )

    # Keep safely under free-tier RPM
    time.sleep(5)

# ==========================================
# FINISH
# ==========================================

print("\n========================================")
print("Enrichment completed.")
print("========================================")

remaining = 0

for verse in dataset:

    if not verse.get("summary"):
        remaining += 1

print(f"Processed in this run : {processed_this_run}")
print(f"Remaining verses      : {remaining}")
print(f"Output file           : {OUTPUT_FILE}")

if remaining == 0:
    print("\n🎉 All verses have been enriched successfully!")
else:
    print("\nYou can run this script again anytime.")
    print("It will automatically resume from where it stopped.")