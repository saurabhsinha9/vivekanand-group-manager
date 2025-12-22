
FROM alpine:3.20

RUN apk add --no-cache postgresql16-client bash curl tzdata ca-certificates \
    && update-ca-certificates

# Install rclone
RUN curl -fsSL https://rclone.org/install.sh | bash

ENV TZ=Asia/Kolkata

WORKDIR /app
COPY backup.sh /app/backup.sh
RUN chmod +x /app/backup.sh

# rclone config will be provided via env
ENV RCLONE_CONFIG=/app/rclone.conf

CMD ["/bin/bash", "/app/backup.sh"]
