#!/usr/bin/env bash

# Configure local developer certificates for Ro-Co
# - Runs db/Certs.sh to generate self-signed dev certs
# - Copies RoCoAPI.pem and RoCoRootCA.pem into restAPI/src/main/resources/certs
# - Overwrites existing files and creates folders if needed
#
# IMPORTANT: This is a developer setup using self-signed certificates.
# DO NOT use these certs for production or publish them.

set -euo pipefail

RED="\033[0;31m"
YELLOW="\033[1;33m"
GREEN="\033[0;32m"
NC="\033[0m" # No Color

fail() {
	echo -e "${RED}Error:${NC} $*" >&2
	exit 1
}

info() {
	echo -e "${GREEN}==>${NC} $*"
}

warn() {
	echo -e "${YELLOW}WARNING:${NC} $*"
}

# Resolve repository root (directory containing this script)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$SCRIPT_DIR"

DB_CERTS_SCRIPT="$REPO_ROOT/db/Certs.sh"
DB_OUT_DIR="$REPO_ROOT/db/out"
DEST_DIR_MAIN="$REPO_ROOT/restAPI/src/main/resources/certs"

API_PEM="RoCoAPI.pem"
ROOT_CA_PEM="RoCoRootCA.pem"

echo
warn "Developer setup: Using self-signed certificates. Never publish or use in production!"
echo

# Check prerequisites
[[ -x "$DB_CERTS_SCRIPT" ]] || fail "db/Certs.sh not found or not executable at: $DB_CERTS_SCRIPT"

info "Running certificate generation: $DB_CERTS_SCRIPT"
"$DB_CERTS_SCRIPT"

info "Verifying generated files in: $DB_OUT_DIR"
[[ -f "$DB_OUT_DIR/$API_PEM" ]] || fail "Missing $API_PEM in $DB_OUT_DIR (generation failed?)"
[[ -f "$DB_OUT_DIR/$ROOT_CA_PEM" ]] || fail "Missing $ROOT_CA_PEM in $DB_OUT_DIR (generation failed?)"

info "Preparing destination folder: $DEST_DIR_MAIN"
mkdir -p "$DEST_DIR_MAIN"

info "Copying $API_PEM -> $DEST_DIR_MAIN/$API_PEM (overwrite)"
cp -f "$DB_OUT_DIR/$API_PEM" "$DEST_DIR_MAIN/$API_PEM"

info "Copying $ROOT_CA_PEM -> $DEST_DIR_MAIN/$ROOT_CA_PEM (overwrite)"
cp -f "$DB_OUT_DIR/$ROOT_CA_PEM" "$DEST_DIR_MAIN/$ROOT_CA_PEM"

echo
info "Certificates placed in application resources:"
ls -l "$DEST_DIR_MAIN/$API_PEM" "$DEST_DIR_MAIN/$ROOT_CA_PEM" || true

echo
warn "These self-signed developer certificates are for local development only."
warn "Do NOT commit/publish real private keys or use these in production environments."
echo
info "Done."

