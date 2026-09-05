import psycopg2
from faker import Faker
from datetime import datetime
from tqdm import tqdm
import uuid

fake = Faker()

conn = psycopg2.connect(
    host="localhost",
    port=5432,
    dbname="sw",
    user="postgres",
    password="admin"
)
cur = conn.cursor()

BATCH_SIZE = 10_000
TOTAL = 1_000_000

batch = []

with tqdm(total=TOTAL, unit="user", unit_scale=True) as pbar:
    for i in range(TOTAL):
        now = datetime.now()
        batch.append((
            str(uuid.uuid4()),
            fake.unique.email(),
            fake.password(),
            fake.first_name(),
            fake.last_name(),
            True,
            now,
            now
        ))

        if len(batch) == BATCH_SIZE:
            cur.executemany("""
                INSERT INTO users (id, email, password, first_name, last_name, is_active, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """, batch)
            conn.commit()
            pbar.update(len(batch))
            batch = []

    if batch:
        cur.executemany("""
            INSERT INTO users (id, email, password, first_name, last_name, is_active, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """, batch)
        conn.commit()
        pbar.update(len(batch))

print("Done!")
cur.close()
conn.close()
