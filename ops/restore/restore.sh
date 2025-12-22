
#!/usr/bin/env bash
set -euo pipefail

# Env required: NEW_DB_URL, NEW_DB_USER, NEW_DB_PASS, RCLONE_CONFIG, GDRIVE_REMOTE

# Prepare rclone config file
if [ -n "${RCLONE_CONFIG:-}" ]; then
  echo "$RCLONE_CONFIG" > /app/rclone.conf
else
  echo "RCLONE_CONFIG missing"; exit 1
fi

# Parse JDBC URL (jdbc:postgresql://host:port/db?sslmode=require)
JDBC="$NEW_DB_URL"
HOSTPORT=$(echo "$JDBC" | sed -E 's#jdbc:postgresql://([^/]+)/.*#\1#')
HOST=$(echo "$HOSTPORT" | cut -d: -f1)
PORT=$(echo "$HOSTPORT" | cut -d: -f2)
PORT=${PORT:-5432}
DBNAME=$(echo "$JDBC" | sed -E 's#jdbc:postgresql://[^/]+/([^?]+).*#\1#')

echo "Listing backups on $GDRIVE_REMOTE"
LATEST=$(rclone lsf "$GDRIVE_REMOTE" --format p --sort name | tail -n 1)
if [ -z "$LATEST" ]; then
  echo "No backups found"; exit 1
fi

echo "Fetching latest backup: $LATEST"
rclone copy "$GDRIVE_REMOTE/$LATEST" /app/

echo "Restoring into $HOST:$PORT / $DBNAME"
gzip -dc "/app/$LATEST" | PGPASSWORD="$NEW_DB_PASS" psql \
  --host="$HOST" \
  --port="$PORT" \
  --username="$NEW_DB_USER" \
  --dbname="$DBNAME" \
  --set ON_ERROR_STOP=on

echo "Restore complete."
