#!/usr/bin/env bash
# check-compiler-warnings.sh
#
# Reads Kotlin compiler output from stdin (or a file passed as $1),
# compares every "w: " warning line against the baseline file, and
# exits non-zero if any warning is NOT covered by the baseline.
#
# Usage:
#   ./gradlew build 2>&1 | bash .github/check-compiler-warnings.sh
#   bash .github/check-compiler-warnings.sh build_log.txt
#
# The baseline file is expected at: .github/compiler-warnings-baseline.txt

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASELINE="$SCRIPT_DIR/compiler-warnings-baseline.txt"

if [[ ! -f "$BASELINE" ]]; then
  echo "❌ Baseline file not found: $BASELINE"
  exit 1
fi

# Read input from file argument or stdin
if [[ $# -ge 1 && -f "$1" ]]; then
  INPUT="$1"
else
  INPUT="/dev/stdin"
fi

# Collect all "w: " warning lines from the build output
ALL_WARNINGS=$(grep "^w: " "$INPUT" || true)

if [[ -z "$ALL_WARNINGS" ]]; then
  echo "✅ No compiler warnings found."
  exit 0
fi

# Load baseline patterns (skip blank lines and comment lines starting with #)
mapfile -t BASELINE_PATTERNS < <(grep -v '^\s*#' "$BASELINE" | grep -v '^\s*$')

NEW_WARNINGS=()

while IFS= read -r warning; do
  matched=false
  for pattern in "${BASELINE_PATTERNS[@]}"; do
    if [[ "$warning" == *"$pattern"* ]]; then
      matched=true
      break
    fi
  done
  if [[ "$matched" == false ]]; then
    NEW_WARNINGS+=("$warning")
  fi
done <<< "$ALL_WARNINGS"

BASELINE_COUNT="${#BASELINE_PATTERNS[@]}"
TOTAL_COUNT=$(echo "$ALL_WARNINGS" | wc -l | tr -d ' ')
NEW_COUNT="${#NEW_WARNINGS[@]}"
KNOWN_COUNT=$(( TOTAL_COUNT - NEW_COUNT ))

echo "Compiler warnings: $TOTAL_COUNT total, $KNOWN_COUNT matched baseline, $NEW_COUNT new."

if [[ "$NEW_COUNT" -gt 0 ]]; then
  echo ""
  echo "❌ Found $NEW_COUNT new compiler warning(s) not in the baseline:"
  echo "   Add them to .github/compiler-warnings-baseline.txt if they are acceptable,"
  echo "   or fix the underlying issue."
  echo ""
  for w in "${NEW_WARNINGS[@]}"; do
    echo "  $w"
  done
  exit 1
fi

echo "✅ All warnings are accounted for in the baseline."

