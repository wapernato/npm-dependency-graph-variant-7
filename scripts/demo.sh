#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT"
if [ ! -x tools/d2 ]; then sh scripts/install-d2.sh; fi
for PACKAGE in is-odd@3.0.1 is-even@1.0.0 ansi-styles@4.3.0; do
    NAME=${PACKAGE%@*}
    ./run.sh --package "$PACKAGE" --ascii --d2 "$ROOT/tools/d2" --output "output/$NAME.svg"
    ./run.sh --package "$PACKAGE" --mode order
done
./run.sh --package A --test --repository examples/cycle.json --ascii \
    --d2 "$ROOT/tools/d2" --output output/cycle.svg
