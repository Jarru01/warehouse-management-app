# Backup container: PostgreSQL client tools of the same major version as the
# database, plus rclone for optional off-site copies.
FROM postgres:17-alpine

RUN apk add --no-cache rclone

COPY zaloha.sh /usr/local/bin/zaloha
# Strip Windows line endings in case the script was checked out with CRLF.
RUN sed -i 's/\r$//' /usr/local/bin/zaloha && chmod 755 /usr/local/bin/zaloha

# The script starts as root only to take ownership of the mounted backup
# directory, then continues as the unprivileged postgres user (gosu).
ENTRYPOINT ["/usr/local/bin/zaloha"]
CMD ["planovac"]
