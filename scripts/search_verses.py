import os

import psycopg2
from dotenv import load_dotenv
from google import genai


# ==========================================
# CONFIG
# ==========================================

load_dotenv()

DB_HOST = os.getenv("POSTGRES_HOST", "localhost")
DB_PORT = os.getenv("POSTGRES_PORT", "5432")
DB_NAME = os.getenv("POSTGRES_DB", "gita_ai")
DB_USER = os.getenv("POSTGRES_USER", "postgres")
DB_PASSWORD = os.getenv("POSTGRES_PASSWORD")

EMBEDDING_MODEL = "gemini-embedding-2"

TOP_K = 5


# ==========================================
# CLIENTS
# ==========================================

if not DB_PASSWORD:
    raise Exception("POSTGRES_PASSWORD not found in .env")

gemini = genai.Client(
    api_key=os.getenv("GEMINI_API_KEY")
)


# ==========================================
# CREATE QUERY EMBEDDING
# ==========================================

def create_embedding(query):

    response = gemini.models.embed_content(
        model=EMBEDDING_MODEL,
        contents=query
    )

    return response.embeddings[0].values


# ==========================================
# SEARCH DATABASE
# ==========================================

def search_verses(query, top_k=TOP_K):

    embedding = create_embedding(query)

    connection = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )

    cursor = connection.cursor()

    sql = """
        SELECT
            id,
            chapter,
            verse,
            translation,
            summary,
            keywords,
            topics,
            life_situations,
            emotions,
            questions,
            1 - (embedding <=> %s::vector) AS similarity
        FROM gita_verses
        ORDER BY embedding <=> %s::vector
        LIMIT %s;
    """

    vector = "[" + ",".join(map(str, embedding)) + "]"

    cursor.execute(
        sql,
        (vector, vector, top_k)
    )

    results = cursor.fetchall()

    cursor.close()
    connection.close()

    return results


# ==========================================
# MAIN
# ==========================================

if __name__ == "__main__":

    query = input("\nAsk something: ").strip()

    if not query:
        print("Please enter a question.")
        exit()

    print("\nSearching Bhagavad Gita...\n")

    try:

        results = search_verses(query)

        if not results:
            print("No results found.")
            exit()

        for rank, row in enumerate(results, start=1):

            (
                verse_id,
                chapter,
                verse,
                translation,
                summary,
                keywords,
                topics,
                life_situations,
                emotions,
                questions,
                similarity
            ) = row

            print("=" * 70)

            print(
                f"{rank}. {verse_id} "
                f"(Chapter {chapter}, Verse {verse})"
            )

            print(
                f"Similarity: {similarity:.4f}"
            )

            print(
                f"\nTranslation:\n{translation}"
            )

            print(
                f"\nSummary:\n{summary}"
            )

            print(
                f"\nTopics: {topics}"
            )

        print("=" * 70)

    except Exception as e:

        print("\n❌ Search failed.")
        print(e)