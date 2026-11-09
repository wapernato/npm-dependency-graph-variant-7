#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
sh "$ROOT/scripts/build.sh"
exec node "$ROOT/tests/compare-npm.cjs"
