#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
sh "$ROOT/scripts/build.sh"
JAVA=java
JAVAC=javac
if [ -n "${JAVA_HOME:-}" ]; then JAVA="$JAVA_HOME/bin/java"; JAVAC="$JAVA_HOME/bin/javac"; fi
mkdir -p "$ROOT/build/tests"
find "$ROOT/tests" -name '*.java' | sed 's/.*/"&"/' > "$ROOT/build/tests.txt"
"$JAVAC" --release 17 -encoding UTF-8 -cp "$ROOT/build/classes" -d "$ROOT/build/tests" @"$ROOT/build/tests.txt"
cd "$ROOT"
"$JAVA" -cp "$ROOT/build/classes:$ROOT/build/tests" ru.mirea.graph.TestSuite
