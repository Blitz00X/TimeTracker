#!/bin/sh

set -e

check_java() {
  if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java executable not found in PATH. Install JDK 21 or newer and try again." >&2
    exit 1
  fi

  version_str=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | head -n 1)
  if [ -z "$version_str" ]; then
    echo "Warning: Unable to determine Java version; proceeding assuming it is adequate." >&2
    return
  fi

  major=$(printf "%s" "$version_str" | awk -F. '{print $1}')
  if [ "$major" = "1" ]; then
    major=$(printf "%s" "$version_str" | awk -F. '{print $2}')
  fi

  if [ "$major" -lt 21 ]; then
    echo "Error: Java $version_str detected. Please use JDK 21 or newer." >&2
    exit 1
  fi
}

cd "$(dirname "$0")"
check_java

echo "Starting TimeTracker+ with Maven Wrapper..."
./mvnw -q -DskipTests javafx:run
