#!/bin/sh
set -e

# Default to 8080 if PORT is not set or empty
PORT="${PORT:-8080}"

# Validate that PORT is numeric
if ! echo "$PORT" | grep -qE '^[0-9]+$'; then
    echo "[docker-entrypoint] Invalid PORT='$PORT', falling back to default 8080"
    PORT=8080
fi

echo "[docker-entrypoint] Configuring Tomcat to bind on 0.0.0.0:${PORT}..."

# Update connector port and address in server.xml dynamically
if [ -f "/usr/local/tomcat/conf/server.xml" ]; then
    sed -i -E "s/<Connector[[:space:]]+port=\"[0-9]+\"/<Connector port=\"${PORT}\" address=\"0.0.0.0\"/g" /usr/local/tomcat/conf/server.xml
fi

# Expose PORT to Tomcat system properties as additional safety
export CATALINA_OPTS="${CATALINA_OPTS} -DPORT=${PORT} -Dport.http=${PORT}"

echo "[docker-entrypoint] Starting Tomcat with: $@"
exec "$@"
