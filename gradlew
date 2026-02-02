#!/bin/bash
set -euo pipefail

DEFAULT_GRADLE_VERSION="8.11.1"
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER_PROPS="${SCRIPT_DIR}/gradle/wrapper/gradle-wrapper.properties"
GRADLE_DISTS_DIR="${HOME}/.gradle/wrapper/dists"

read_wrapper_distribution_url() {
    if [ -f "$WRAPPER_PROPS" ]; then
        # Example in gradle-wrapper.properties: https\://... -> https://...
        local url
        url="$(sed -n 's/^distributionUrl=//p' "$WRAPPER_PROPS" | head -n 1)"
        url="${url//\\/}"
        if [ -n "$url" ]; then
            printf '%s' "$url"
            return 0
        fi
    fi
    return 1
}

extract_gradle_version() {
    local url="$1"
    if [[ "$url" =~ gradle-([0-9]+\\.[0-9]+(\\.[0-9]+)?(-[^/]+)?) ]]; then
        printf '%s' "${BASH_REMATCH[1]}"
        return 0
    fi
    printf '%s' "$DEFAULT_GRADLE_VERSION"
}

DIST_URL="${GRADLE_DISTRIBUTION_URL:-$(read_wrapper_distribution_url || true)}"
DIST_URL="${DIST_URL:-https://services.gradle.org/distributions/gradle-${DEFAULT_GRADLE_VERSION}-bin.zip}"
GRADLE_VERSION="$(extract_gradle_version "$DIST_URL")"
GRADLE_DIR="${GRADLE_DISTS_DIR}/gradle-${GRADLE_VERSION}"

if [ ! -x "${GRADLE_DIR}/bin/gradle" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "$GRADLE_DISTS_DIR"
    TMP_ZIP="$(mktemp "/tmp/gradle-${GRADLE_VERSION}-bin.XXXXXX.zip")"
    cleanup() { rm -f "$TMP_ZIP"; }
    trap cleanup EXIT
    curl --fail --location --retry 3 --retry-all-errors --connect-timeout 10 --progress-bar \
        "$DIST_URL" -o "$TMP_ZIP"
    unzip -oq "$TMP_ZIP" -d "$GRADLE_DISTS_DIR"
fi

exec "${GRADLE_DIR}/bin/gradle" "$@"
