#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
JAVAC=javac
if [ -n "${JAVA_HOME:-}" ]; then JAVAC="$JAVA_HOME/bin/javac"; fi
mkdir -p "$ROOT/build/classes"
find "$ROOT/src" -name '*.java' | sed 's/.*/"&"/' > "$ROOT/build/sources.txt"
"$JAVAC" --release 17 -encoding UTF-8 -d "$ROOT/build/classes" @"$ROOT/build/sources.txt"
