#!/bin/bash

echo "🛰 Updating API Docs..."

SPEC_FILE_URL=https://raw.githubusercontent.com/signum-network/signum-node/main/openapi/dist/signum-api.json
OPENAPI_DIR=./html/api-doc
if [[ ! -e $OPENAPI_DIR ]]; then
  echo "Cannot find $OPENAPI_DIR"
  echo "🚫 Please run this script in your signum-node root dir (aside signum-node executable)"
  exit 1
fi

curl -s $SPEC_FILE_URL -o $OPENAPI_DIR/signum-api.json

echo "✅ API Docs have been updated."
