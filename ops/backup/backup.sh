
#!/usr/bin/env bash
set -euo pipefail

# Required env: DB_URL, DB_USER, DB_PASS, RCLONE_CONFIG, GDRIVE_REMOTE, BACKUP_RETENTION_DAYS

# Prepare rclone config file
if [ -n "${RCLONE_CONFIG:-}" ]; then
  echo "$RCLONE_CONFIG" > /app/rclone.conf
else
  echo "RCLONE_CONFIG missing"; exit 1
fi

# Parse JDBC URL (jdbc:postgresql://host:port/db?sslmode=require)
JDBC="$DB_URL"
HOSTPORT=$(echo "$JDBC" | sed -E 's#jdbc:postgresql://([^/]+)/.*#\1#')
HOST=$(echo "$HOSTPORT" | cut -d: -f1)
PORT=$(echo "$HOSTPORT" | cut -d: -f2)
PORT=${PORT:-5432}
DBNAME=$(echo "$JDBC" | sed -E 's#jdbc:postgresql://[^/]+/([^?]+).*#\1#')

DATE=$(date +'%Y-%m-%d_%H-%M-%S')
BACKUP_FILE="vgm_${DBNAME}_${DATE}.sql.gz"

echo "Starting pg_dump: host=$HOST port=$PORT db=$DBNAME"
PGPASSWORD="$DB_PASS" pg_dump \
  --host="$HOST" \
  --port="$PORT" \
  --username="$DB_USER" \
  --dbname="$DBNAME" \
  --format=plain \
  --no-owner \
  --no-privileges \
  | gzip > "/app/${BACKUP_FILE}"

echo "Uploading to Google Drive: $GDRIVE_REMOTE/${BACKUP_FILE}"
rclone copy "/app/${BACKUP_FILE}" "$GDRIVE_REMOTE" --progress

# Retention/pruning
if [ -n "${BACKUP_RETENTION_DAYS:-}" ]; then
  echo "Pruning backups older than $BACKUP_RETENTION_DAYS days"
  rclone delete "$GDRIVE_REMOTE" --min-age "${BACKUP_RETENTION_DAYS}d"
  rclone rmdirs "$GDRIVE_REMOTE" --leave-root
fi

echo "Backup complete."
