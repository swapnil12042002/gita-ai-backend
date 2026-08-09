from datasets import load_dataset
import json
import os

dataset = load_dataset(
    "Voider22/bhagavad-gita-verses-sanskrit-translations"
)

os.makedirs("data/raw", exist_ok=True)

output_file = "data/raw/gita.json"

with open(output_file, "w", encoding="utf-8") as f:
    json.dump(dataset["train"].to_list(), f, ensure_ascii=False, indent=2)

print(f"Downloaded {len(dataset['train'])} verses.")
print(f"Saved to: {output_file}")