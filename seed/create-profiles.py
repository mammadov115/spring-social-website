import psycopg2
from tqdm import tqdm
import uuid
from datetime import datetime

conn = psycopg2.connect(
    host="localhost",
    port=5432,
    dbname="sw",
    user="postgres",
    password="admin"
)
cur = conn.cursor()

cur.execute("SELECT id FROM users WHERE NOT EXISTS (SELECT 1 FROM profiles p WHERE p.user_id = users.id)")
user_ids = [row[0] for row in cur.fetchall()]

print(f"Profile yaradılacaq user sayı: {len(user_ids)}")

BATCH_SIZE = 1000
now = datetime.now()

for i in tqdm(range(0, len(user_ids), BATCH_SIZE), desc="Profiles yaradılır"):
    batch = user_ids[i:i+BATCH_SIZE]
    values = [(str(uuid.uuid4()), str(uid), False, False, True, now, now) for uid in batch]
    cur.executemany(
        "INSERT INTO profiles (id, user_id, is_email_public, is_birth_date_public, is_location_public, created_at, updated_at) VALUES (%s, %s, %s, %s, %s, %s, %s)",
        values
    )
    conn.commit()

cur.close()
conn.close()
print("Tamamlandı!")