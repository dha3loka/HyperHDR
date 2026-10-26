#!/usr/bin/env bash
set -e
TV_IP="${1:-}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TARBALL="$(cd "$SCRIPT_DIR/.." && pwd)/hyperhdr-termux-v24.0.0-beta.0.tar.gz"

if [ -n "$TV_IP" ]; then
    adb connect "$TV_IP"
fi

echo "Pushing hyperhdr-termux-v24.0.0-beta.0.tar.gz to /sdcard/Download/ on Android TV..."
adb push "$TARBALL" /sdcard/Download/hyperhdr-termux-v24.0.0-beta.0.tar.gz

echo ""
echo "Now open Termux on your Android TV (or via SSH) and run:"
echo "  tar -xzf /sdcard/Download/hyperhdr-termux-v24.0.0-beta.0.tar.gz -C ~"
echo "  ~/termux/install-in-termux.sh"
echo "  hyperhdr"
