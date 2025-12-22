
FROM alpine:3.20

RUN apk add --no-cache postgresql16-client bash curl tzdata ca-certificates \
    && update-ca-certificates

# Install rclone
RUN curl -fsSL https://rclone.org/install.sh | bash
ENV TZ=Asia/Kolkata

WORKDIR /app
COPY restore.sh /app/restore.sh
RUN chmod +x /app/restore.sh

ENV RCLONE_CONFIG=/app/rclone.conf
CMD ["/bin/bash", "/app/restore.sh"]
