#!/usr/bin/env bash
# Installer for Headless HyperHDR v24.0.0-beta.0 in Termux
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
HYPERHDR_HOME="$PREFIX/opt/hyperhdr"
INSTALL_MODE="${1:---auto}"

echo "================================================================"
echo "  Installing HyperHDR v24.0.0-beta.0 Headless Server into Termux"
echo "================================================================"

if command -v pkg >/dev/null 2>&1; then
    echo "[1/4] Checking Termux dependencies..."
    if ! command -v python3 >/dev/null 2>&1; then
        pkg update -y && pkg install -y python
    fi
fi

echo "[2/4] Copying HyperHDR v24.0.0-beta.0 binaries, libraries, and Web UI assets to $HYPERHDR_HOME ..."
rm -rf "$HYPERHDR_HOME"
mkdir -p "$HYPERHDR_HOME" "$PREFIX/bin"
cp -R "$SCRIPT_DIR/opt/hyperhdr/"* "$HYPERHDR_HOME/"
cp -R "$SCRIPT_DIR/pydaemon" "$HYPERHDR_HOME/"
cp -R "$SCRIPT_DIR/shim" "$HYPERHDR_HOME/"
chmod +x "$HYPERHDR_HOME/usr/bin/hyperhdr" "$HYPERHDR_HOME/pydaemon/hyperhdr_termux_daemon.py"

if [ "$INSTALL_MODE" = "--glibc" ] && command -v pkg >/dev/null 2>&1; then
    echo "[3/4] Setting up native Termux glibc-repo runner + libdbus-1.so.3 & libsystemd.so.0 shims..."
    pkg install -y glibc-repo clang
    pkg install -y glibc gcc-libs-glibc glib-glibc
    clang -shared -fPIC -O2 \
        -Wl,-soname,libdbus-1.so.3 \
        -Wl,--version-script="$HYPERHDR_HOME/shim/libdbus.map" \
        "$HYPERHDR_HOME/shim/libdbus_shim.c" \
        -o "$HYPERHDR_HOME/usr/lib/hyperhdr/external/libdbus-1.so.3"
    clang -shared -fPIC -O2 \
        -Wl,-soname,libsystemd.so.0 \
        -Wl,--version-script="$HYPERHDR_HOME/shim/libsystemd.map" \
        "$HYPERHDR_HOME/shim/libsystemd_shim.c" \
        -o "$HYPERHDR_HOME/usr/lib/hyperhdr/external/libsystemd.so.0"
    echo "Compiled native aarch64 libdbus-1.so.3 and libsystemd.so.0 shims."
elif [ "$INSTALL_MODE" = "--proot" ] && command -v pkg >/dev/null 2>&1; then
    echo "[3/4] Setting up proot-distro Debian arm64 with HyperHDR v24.0.0-beta.0 .deb..."
    pkg install -y proot-distro
    proot-distro install debian || true
    DEB_FILE="$(find "$SCRIPT_DIR/.." "$SCRIPT_DIR" -name "HyperHDR-24.0.0-beta.0-Linux-aarch64.deb" 2>/dev/null | head -n 1)"
    if [ -n "$DEB_FILE" ]; then
        cp "$DEB_FILE" "$PREFIX/var/lib/proot-distro/installed-rootfs/debian/root/"
        proot-distro login debian -- bash -c "apt-get update && apt-get install -y /root/HyperHDR-24.0.0-beta.0-Linux-aarch64.deb"
    fi
else
    echo "[3/4] Configured instant Termux Headless engine with Android screen grabber (use './install-in-termux.sh --glibc' for glibc ELF mode)."
fi

echo "[4/4] Installing 'hyperhdr' command to $PREFIX/bin/hyperhdr ..."
cp "$SCRIPT_DIR/bin/hyperhdr" "$PREFIX/bin/hyperhdr"
chmod +x "$PREFIX/bin/hyperhdr"

# Also create Termux:Boot auto-start script
mkdir -p "$HOME/.termux/boot"
cat > "$HOME/.termux/boot/start-hyperhdr.sh" << 'BOOTEOF'
#!/data/data/com.termux/files/usr/bin/sh
termux-wake-lock
/data/data/com.termux/files/usr/bin/hyperhdr start
BOOTEOF
chmod +x "$HOME/.termux/boot/start-hyperhdr.sh"

echo ""
echo "================================================================"
echo "  HyperHDR v24.0.0-beta.0 is installed!"
echo "  Run in foreground : hyperhdr"
echo "  Run in background : hyperhdr start"
echo "  Check status      : hyperhdr status"
echo "  View live logs    : hyperhdr logs"
echo "  Stop daemon       : hyperhdr stop"
echo "  Web UI            : http://127.0.0.1:8090"
echo "================================================================"
