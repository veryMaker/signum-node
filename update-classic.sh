#!/bin/bash

set -e

UI_DIR=./html/ui
if [[ ! -e $UI_DIR ]]; then
  echo "Cannot find $UI_DIR"
  echo "🚫 Please run this script in your signum-node root dir (aside signum-node executable)"
  exit 1
fi

echo "🛰 Updating Signum Classic Wallet to current release..."
echo "⬇️  Downloading latest release..."
git clone --depth 1 https://github.com/signum-network/signum-classic-wallet.git

echo "📝 Updating code base..."
cp -r signum-classic-wallet/src/* /signum/html/ui/classic/

echo "🛀 Cleaning up..."
rm -rf signum-classic-wallet

echo "✅ Classic Wallet has been updated."

