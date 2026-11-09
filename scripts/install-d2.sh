#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
VERSION=v0.9.0
case "$(uname -s)" in Darwin) OS=macos ;; Linux) OS=linux ;; *) echo "Use D2 CLI for your OS" >&2; exit 2 ;; esac
case "$(uname -m)" in arm64|aarch64) ARCH=arm64 ;; x86_64|amd64) ARCH=amd64 ;; *) exit 2 ;; esac
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "$TEMP_DIR"' EXIT HUP INT TERM
URL="https://github.com/terrastruct/d2/releases/download/$VERSION/d2-$VERSION-$OS-$ARCH.tar.gz"
curl --fail --location --retry 2 "$URL" -o "$TEMP_DIR/d2.tar.gz"
tar -xzf "$TEMP_DIR/d2.tar.gz" -C "$TEMP_DIR" "d2-$VERSION/bin/d2"
mkdir -p "$ROOT/tools"
cp "$TEMP_DIR/d2-$VERSION/bin/d2" "$ROOT/tools/d2"
chmod +x "$ROOT/tools/d2"
"$ROOT/tools/d2" --version
