#!/bin/bash
source /etc/profile
unset CDPATH
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASEDIR="$(cd "$SCRIPT_DIR/.." && pwd)"
mkdir -p "$BASEDIR/logs" "$BASEDIR/run"

if [ "${1:-}" = "--configtest" ]; then
    CONF_DIR="$BASEDIR/conf"
    if [ "${2:-}" = "-f" ] && [ -n "${3:-}" ]; then
        CONF_DIR="$3"
    fi

    if [ ! -f "$CONF_DIR/application.properties" ]; then
        echo "Missing application.properties: $CONF_DIR/application.properties" >&2
        exit 1
    fi

    SERVER_PORT=$(awk -F= '$1 ~ /^[[:space:]]*server[.]port[[:space:]]*$/ {gsub(/[[:space:]]/, "", $2); print $2; exit}' "$CONF_DIR/application.properties")
    if [[ ! "$SERVER_PORT" =~ ^[0-9]+$ ]]; then
        echo "Invalid server.port in $CONF_DIR/application.properties" >&2
        exit 1
    fi

    if [ ! -d "$BASEDIR/dtstack" ] || [ ! -d "$BASEDIR/lib" ]; then
        echo "Missing backend directories under $BASEDIR" >&2
        exit 1
    fi

    if ! compgen -G "$BASEDIR/dtstack/*.jar" > /dev/null; then
        echo "Missing backend JAR under $BASEDIR/dtstack" >&2
        exit 1
    fi

    echo "Configuration OK (server.port=$SERVER_PORT)"
    exit 0
fi

cd "$BASEDIR" || exit 1
exec java ${JAVA_OPTS:-} \
    -Duser.dir.conf="$BASEDIR/conf" \
    -Duser.dir.logs="$BASEDIR/logs" \
    -cp "$BASEDIR/dtstack/*:$BASEDIR/lib/*" \
    com.dtstack.sqlparserx.SqlParserXWebApplication "$@"
