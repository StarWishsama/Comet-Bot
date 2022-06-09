#!/usr/bin/env bash

if ! [ -x "$(command -v ktlint)" ]; then
  echo 'Error: ktlint is not installed.'
  echo 'You can download from https://ktlint.github.io/ or install via your package manager.'
  exit 1
fi

ktlint -F \
  '**/*.kt' \
  '**/*.kts' \
  '!**/generated/**' \
  '!**/build/**' \
  --reporter=checkstyle,output=build/ktlint-report.xml
