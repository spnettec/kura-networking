#!/usr/bin/env bash
# Regenerate the org.freedesktop.* and fi.w1.* dbus-java 5.2.0 bindings
# consumed by bundles/org.eclipse.kura.nm.
#
# Pinned versions (override via NM_TAG / MM_TAG env vars):
#   NetworkManager 1.46.0   — upstream XML at gitlab.freedesktop.org
#   ModemManager mm-1-22    — upstream XML at gitlab.freedesktop.org
#   wpa_supplicant 2.10     — no upstream XML ships with the tarball;
#                             root XML is read from .nm-regen/xml-wpa/ (VM capture),
#                             and fi/w1/wpa_supplicant1/Interface.java is preserved
#                             through the regen as a hand-port (no source XML).
#
# Flags:
#   SKIP_FETCH=1   reuse already-cached upstream XMLs
#   DRY_RUN=1      stage only; do not touch bundles/
#
# Layout:
#   $WORKSPACE/.dbus-gen/lib/        generator classpath jars (populated lazily)
#   $WORKSPACE/.nm-regen/upstream/   fetched XMLs (cached, git-ignored)
#   $WORKSPACE/.nm-regen/xml-wpa/    VM-captured wpa_supplicant1 root XML
#   $WORKSPACE/.nm-regen/xml-stripped/  XMLs with <node name=".."> normalised
#   $WORKSPACE/.nm-regen/staged/     generator output, before rsync

set -euo pipefail

NM_TAG="${NM_TAG:-1.46.0}"
MM_TAG="${MM_TAG:-mm-1-22}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
WORKSPACE_ROOT="$(cd "$REPO_ROOT/.." && pwd)"

CACHE_ROOT="$WORKSPACE_ROOT/.nm-regen"
DBUS_GEN_ROOT="$WORKSPACE_ROOT/.dbus-gen"
LIB_DIR="$DBUS_GEN_ROOT/lib"
BINDING_ROOT="$REPO_ROOT/bundles/org.eclipse.kura.nm/src/main/java"

UPSTREAM_NM="$CACHE_ROOT/upstream/nm-$NM_TAG"
UPSTREAM_MM="$CACHE_ROOT/upstream/mm-$MM_TAG"
WPA_XML="$CACHE_ROOT/xml-wpa/fi_w1_wpa_supplicant1.xml"

STAGE_DIR="$CACHE_ROOT/staged"
STRIP_DIR="$CACHE_ROOT/xml-stripped"

log() { printf '[regen] %s\n' "$*" >&2; }
die() { printf '[regen] error: %s\n' "$*" >&2; exit 1; }

for cmd in curl java rsync python3 sed find; do
    command -v "$cmd" >/dev/null || die "missing required command: $cmd"
done

if [[ ! -d "$LIB_DIR" || -z "$(find "$LIB_DIR" -maxdepth 1 -name '*.jar' -print -quit 2>/dev/null)" ]]; then
    log "generator jars missing at $LIB_DIR — running 'mvn dependency:copy-dependencies'"
    command -v mvn >/dev/null || die "mvn not on PATH and $LIB_DIR is empty"
    mkdir -p "$LIB_DIR"
    (cd "$DBUS_GEN_ROOT" && mvn -q dependency:copy-dependencies -DoutputDirectory="$LIB_DIR" -DincludeScope=runtime)
fi

CP=$(find "$LIB_DIR" -maxdepth 1 -name '*.jar' ! -name '*native*' | paste -sd: -)
[[ -n "$CP" ]] || die "no jars in $LIB_DIR after population"

[[ -f "$WPA_XML" ]] || die "wpa_supplicant XML not found at $WPA_XML (VM capture required — wpa_supplicant 2.10 has no upstream introspection XML)"

YEAR=$(date +%Y)
EPL_HEADER="/*******************************************************************************
 * Copyright (c) 2023, $YEAR Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 *******************************************************************************/"

fetch_gitlab_dir() {
    local project_path_enc="$1" ref="$2" subdir="$3" out_dir="$4" raw_base="$5"
    mkdir -p "$out_dir"
    local listing
    listing=$(curl -fsSL "https://gitlab.freedesktop.org/api/v4/projects/${project_path_enc}/repository/tree?path=${subdir}&ref=${ref}&per_page=100") \
        || die "gitlab API list failed for $project_path_enc @ $ref"
    local names
    names=$(printf '%s' "$listing" | python3 -c 'import json,sys
for x in json.load(sys.stdin):
    if x.get("type") == "blob" and x["name"].endswith(".xml") and not x["name"].startswith("wip-") and x["name"] != "all.xml":
        print(x["name"])')
    [[ -n "$names" ]] || die "empty xml list for $project_path_enc"
    local n
    while IFS= read -r n; do
        [[ -n "$n" ]] || continue
        if [[ -s "$out_dir/$n" ]]; then continue; fi
        log "fetch $n"
        curl -fsSL "${raw_base}/${ref}/${subdir}/${n}" -o "$out_dir/$n" \
            || die "fetch failed: $n"
    done <<< "$names"
}

if [[ "${SKIP_FETCH:-0}" != "1" ]]; then
    log "fetching NetworkManager $NM_TAG"
    fetch_gitlab_dir 'NetworkManager%2FNetworkManager' "$NM_TAG" 'introspection' "$UPSTREAM_NM" \
        'https://gitlab.freedesktop.org/NetworkManager/NetworkManager/-/raw'
    log "fetching ModemManager $MM_TAG"
    fetch_gitlab_dir 'mobile-broadband%2FModemManager' "$MM_TAG" 'introspection' "$UPSTREAM_MM" \
        'https://gitlab.freedesktop.org/mobile-broadband/ModemManager/-/raw'
else
    log "SKIP_FETCH=1 — reusing cached XMLs"
    [[ -d "$UPSTREAM_NM" ]] || die "cache miss: $UPSTREAM_NM"
    [[ -d "$UPSTREAM_MM" ]] || die "cache miss: $UPSTREAM_MM"
fi

rm -rf "$STAGE_DIR" "$STRIP_DIR"
mkdir -p "$STAGE_DIR" "$STRIP_DIR"

# The 5.2.0 generator validates <node name=".."> against an internal "/" assumption
# when --inputFile is used without positional node arg. Strip the attribute so any XML
# whose root node has a path (upstream NM/MM XMLs do) feeds through cleanly.
generate_one() {
    local xml="$1"
    local stripped="$STRIP_DIR/$(basename "$xml")"
    # Drop name="..." only from the root <node ...> element; the generator validates
    # it against an internal "/" expectation when --inputFile is used. Upstream MM
    # XML also carries xmlns:doc=".." on the same element, so we surgically strip
    # the single attribute instead of rewriting the whole opening tag. The regex
    # is anchored to lines starting with "<node " so <arg name="..."> etc. are left
    # alone.
    sed -E '/^[[:space:]]*<node /s| name="[^"]*"||' "$xml" > "$stripped"
    if ! java -cp "$CP" org.freedesktop.dbus.utils.generator.InterfaceCodeGenerator \
            --inputFile "$stripped" -o "$STAGE_DIR" --all '*' >/dev/null 2>"$STRIP_DIR/${xml##*/}.log"; then
        cat "$STRIP_DIR/${xml##*/}.log" >&2
        die "generator failed on $xml"
    fi
}

log "running generator (NM + MM + wpa)"
shopt -s nullglob
xml_count=0
for xml in "$UPSTREAM_NM"/*.xml "$UPSTREAM_MM"/*.xml "$WPA_XML"; do
    [[ -f "$xml" ]] || continue
    generate_one "$xml"
    xml_count=$((xml_count + 1))
done
log "processed $xml_count xml files"

# The generator emits org/freedesktop/dbus/Peer.java when DBus.Peer is in the input —
# but that interface is already provided by the dbus-java-core jar. Drop it.
rm -f "$STAGE_DIR/org/freedesktop/dbus/Peer.java"
[[ -d "$STAGE_DIR/org/freedesktop/dbus" ]] && rmdir "$STAGE_DIR/org/freedesktop/dbus" 2>/dev/null || true

# dbus-java 5.2.0 generator bug: when a signal's XML arg is named "path" (e.g.
# Voice.CallAdded, Messaging.Added, wpa_supplicant Interface{Added,Removed}), the
# generator emits a constructor with a duplicate `path` parameter and a getPath()
# that clashes with Message.getPath()'s String return type. Rename the DBusPath
# field/arg/getter to "dbusPath" so the file compiles.
fix_path_shadowing() {
    local f="$1"
    grep -q '^[[:space:]]*private final DBusPath path;' "$f" || return 0
    local tmp="$f.tmp"
    sed -E \
        -e 's/private final DBusPath path;/private final DBusPath dbusPath;/g' \
        -e 's/(String path, )DBusPath path/\1DBusPath dbusPath/g' \
        -e 's/super\(path, path/super(path, dbusPath/g' \
        -e 's/this\.path = path;/this.dbusPath = dbusPath;/g' \
        -e 's/public DBusPath getPath\(\)/public DBusPath getDbusPath()/g' \
        -e 's/return path;/return dbusPath;/g' \
        "$f" > "$tmp"
    mv "$tmp" "$f"
}
log "fixing 5.2.0 generator path-arg shadowing"
while IFS= read -r f; do fix_path_shadowing "$f"; done < <(find "$STAGE_DIR" -name '*.java')

# wpa_supplicant Interface has no separate XML upstream; preserve the hand-port.
WPA_IFACE_REL="fi/w1/wpa_supplicant1/Interface.java"
if [[ -f "$BINDING_ROOT/$WPA_IFACE_REL" ]]; then
    mkdir -p "$STAGE_DIR/fi/w1/wpa_supplicant1"
    cp "$BINDING_ROOT/$WPA_IFACE_REL" "$STAGE_DIR/$WPA_IFACE_REL"
else
    log "warn: $WPA_IFACE_REL absent from bundle — not preserving"
fi

stamp_header() {
    local f="$1"
    local first
    first=$(head -1 "$f")
    [[ "$first" == /\*\*\** ]] && return 0
    local tmp="$f.tmp"
    { printf '%s\n' "$EPL_HEADER"; cat "$f"; } > "$tmp"
    mv "$tmp" "$f"
}
log "stamping EPL header"
while IFS= read -r f; do stamp_header "$f"; done < <(find "$STAGE_DIR" -name '*.java')

generated_count=$(find "$STAGE_DIR" -name '*.java' | wc -l | tr -d ' ')
log "$generated_count java files staged at $STAGE_DIR"

if [[ "${DRY_RUN:-0}" == "1" ]]; then
    log "DRY_RUN=1 — leaving bundle untouched. Diff:"
    diff -rq "$STAGE_DIR" "$BINDING_ROOT" 2>/dev/null | grep -E "^Only in $STAGE_DIR|differ$" | head -50 >&2 || true
    exit 0
fi

log "replacing $BINDING_ROOT/{org/freedesktop,fi/w1}"
rm -rf "$BINDING_ROOT/org/freedesktop" "$BINDING_ROOT/fi/w1"
mkdir -p "$BINDING_ROOT"
rsync -a "$STAGE_DIR/" "$BINDING_ROOT/"
find "$BINDING_ROOT/fi" -type d -empty -delete 2>/dev/null || true

final_count=$(find "$BINDING_ROOT/org/freedesktop" "$BINDING_ROOT/fi/w1" -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
log "done: $final_count files in bundle"
