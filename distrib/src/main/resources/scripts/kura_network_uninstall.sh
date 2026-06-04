#!/bin/bash
#
#  Copyright (c) 2020, 2025 Eurotech and/or its affiliates and others
#
#  This program and the accompanying materials are made
#  available under the terms of the Eclipse Public License 2.0
#  which is available at https://www.eclipse.org/legal/epl-2.0/
#
#  SPDX-License-Identifier: EPL-2.0
#
#  Contributors:
#   Eurotech
#

STATUS=$1
BASE_DIR=$2
KURA_SYMLINK=$3

# shellcheck source=/dev/null
source "${BASE_DIR}/${KURA_SYMLINK}/.data/install_network_config.sh"

# Remove this sibling from the kura-core install-order registry, identically to
# every other sibling. Only invoked on a real removal (STATUS=remove).
deregister_sibling() {
    SIBLING_NAME="networking"
    REGISTRY="${BASE_DIR}/${KURA_SYMLINK}/framework/sibling-install-order"
    if [ -f "${REGISTRY}" ]; then
        echo "  Removing ${SIBLING_NAME} from sibling-install-order"
        TMP=$(mktemp 2>/dev/null || echo "${REGISTRY}.tmp.$$")
        grep -vFx "${SIBLING_NAME}" "${REGISTRY}" > "${TMP}" 2>/dev/null || true
        mv -f "${TMP}" "${REGISTRY}" 2>/dev/null || rm -f "${TMP}"
    fi
}

restore_backup_files() {
    SUFFIX="${1}"

    shift

    for file in "${@}"
    do
        if [ -f "${file}" ] && expr "${file}" : ".*[.]${SUFFIX}$" > /dev/null; then
            mv "${file}" "${file%."${SUFFIX}"}"
        fi
    done
}

restore_named_config() {
    [ -f /var/named/named.ca.kurasave ] && mv /var/named/named.ca.kurasave /var/named/named.ca
    [ -f /etc/bind/named.conf.kurasave ] && mv /etc/bind/named.conf.kurasave /etc/bind/named.conf
    [ -f /etc/named.rfc1912.zones.kurasave ] && mv /etc/named.rfc1912.zones.kurasave /etc/named.rfc1912.zones

    if [ -d /etc/apparmor.d ]; then
        rm -f /etc/apparmor.d/usr.sbin.named
    fi
}

restore_netplan() {
  if [ -f /etc/netplan/zz-kura-use-nm.yaml ]; then
    rm -f /etc/netplan/zz-kura-use-nm.yaml
  fi

  restore_backup_files kurasave /{lib,etc}/netplan/*
}

restore_nm_installation() {
    # restore /etc/network/interfaces.kurasave
    if test -f /etc/network/interfaces.kurasave; then
        mv /etc/network/interfaces.kurasave /etc/network/interfaces
    fi

    # restore iptables
    HAD_IPTABLES_BACKUP=0
    if test -f /etc/sysconfig/iptables.kurasave; then
        mv /etc/sysconfig/iptables.kurasave /etc/sysconfig/iptables
        HAD_IPTABLES_BACKUP=1
    fi
    if test -f /proc/sys/net/ipv4/ip_forward.kurasave; then
        mv "${BASE_DIR}/${KURA_SYMLINK}/.data/ip_forward.kurasave" /proc/sys/net/ipv4/ip_forward
    fi
    
    # restore /etc/default/dnsmasq.kurasave
    if [ -f /etc/default/dnsmasq.kurasave ]; then
        mv /etc/default/dnsmasq.kurasave /etc/default/dnsmasq
    fi

    restore_named_config

    restore_netplan
}

recover_dnsmasq_conf_file() {
    DNSMASQ_CONF_FILE="/etc/dnsmasq.conf"
    if [ -f "${DNSMASQ_CONF_FILE}.kurasave" ]; then
        mv "${DNSMASQ_CONF_FILE}.kurasave" "${DNSMASQ_CONF_FILE}"
    fi

    DNSMASQ_LXC_FILE="/etc/dnsmasq.d/lxc"
    if [ -f "/etc/lxc.kurasave" ]; then
        mv "/etc/lxc.kurasave" "${DNSMASQ_LXC_FILE}"
    fi
}

remove_dnsmasq_leases() {
    # Remove dnsmasq leases file if it exists and is owned by 'kurad'
    DNSMASQ_LEASES_FILE="/var/lib/dhcp/dnsmasq.leases"
    if [ -f "${DNSMASQ_LEASES_FILE}" ]; then
        FILE_OWNER=$(stat -c '%U' "${DNSMASQ_LEASES_FILE}" 2>/dev/null)
        if [ "${FILE_OWNER}" = "kurad" ]; then
            rm -f "${DNSMASQ_LEASES_FILE}"
            echo "The dnsmasq leases file has been successfully removed."
        fi
    fi
}

recover_web_ui_kura_properties() {
    if [ -d "${BASE_DIR}/${KURA_SYMLINK}/user" ]; then
        set_kura_property "kura.have.net.admin" "false"
        set_kura_property "kura.have.firewall.admin" "false"
    fi
}

set_kura_property() {
    KEY="$1"
    VALUE="$2"
    # Write to kura_custom.properties (under user/) rather than kura.properties
    # (under framework/). kura_custom.properties overrides kura.properties and
    # is preserved across kura-core upgrades because user/ is part of the
    # upgrade save/restore cycle.
    PROPERTIES_FILE="${BASE_DIR}/${KURA_SYMLINK}/user/kura_custom.properties"

    # Ensure the file exists.
    if [ ! -f "${PROPERTIES_FILE}" ]; then
        mkdir -p "$(dirname "${PROPERTIES_FILE}")"
        touch "${PROPERTIES_FILE}"
    fi

    if grep -q "^#*${KEY}=" "${PROPERTIES_FILE}"; then
        sed -i "s|^#*${KEY}=.*|${KEY}=${VALUE}|" "${PROPERTIES_FILE}"
    else
        echo "${KEY}=${VALUE}" >> "${PROPERTIES_FILE}"
    fi
}

remove_kura_networking_service() {
    if [ -f /usr/lib/systemd/system/kura.service.d/kura-networking.conf ]; then
        rm /usr/lib/systemd/system/kura.service.d/kura-networking.conf
    fi
}

flush_kernel_iptables() {
    # Kura installs custom chains (input-kura, forward-kura, postrouting-kura, ...)
    # and sets default policies to DROP. Restoring the on-disk config file alone
    # leaves the running kernel locked down. Reset policies first so connectivity
    # is preserved during the flush, then drop all rules and user chains.
    if ! command -v iptables > /dev/null 2>&1; then
        return
    fi

    echo "Flushing Kura-managed iptables rules from kernel..."

    iptables -P INPUT ACCEPT 2>/dev/null || true
    iptables -P FORWARD ACCEPT 2>/dev/null || true
    iptables -P OUTPUT ACCEPT 2>/dev/null || true

    for table in filter nat mangle raw; do
        iptables -t "${table}" -F 2>/dev/null || true
        iptables -t "${table}" -X 2>/dev/null || true
    done

    if command -v ip6tables > /dev/null 2>&1; then
        ip6tables -P INPUT ACCEPT 2>/dev/null || true
        ip6tables -P FORWARD ACCEPT 2>/dev/null || true
        ip6tables -P OUTPUT ACCEPT 2>/dev/null || true
        for table in filter mangle raw; do
            ip6tables -t "${table}" -F 2>/dev/null || true
            ip6tables -t "${table}" -X 2>/dev/null || true
        done
    fi
}

reapply_user_iptables_backup() {
    # Only re-apply if restore_nm_installation actually moved a pre-Kura backup
    # back into place. If there was no .kurasave, the file currently on disk is
    # Kura's own iptables-save dump (full of input-kura/forward-kura rules) —
    # restoring it would just put back what we just flushed.
    if [ "${HAD_IPTABLES_BACKUP:-0}" != "1" ]; then
        return
    fi
    if ! command -v iptables-restore > /dev/null 2>&1; then
        return
    fi
    if [ ! -s /etc/sysconfig/iptables ]; then
        return
    fi
    # Defensive: if the install script chained backups (saved a Kura-poisoned
    # file as .kurasave during a re-install), the "backup" is actually a Kura
    # rule dump. Re-applying it would re-create the input-kura/forward-kura/
    # postrouting-kura chains we just flushed. Drop the file instead.
    if grep -qE '^:?(input-kura|forward-kura|output-kura|prerouting-kura|postrouting-kura)' /etc/sysconfig/iptables; then
        echo "Saved iptables backup contains Kura chains; discarding instead of reapplying."
        rm -f /etc/sysconfig/iptables
        return
    fi
    echo "Reapplying pre-Kura iptables backup..."
    iptables-restore < /etc/sysconfig/iptables 2>/dev/null || true
}


kura_uninstall() {
    echo "Uninstalling Kura networking..."

    if [ "${STATUS}" = "remove" ]; then
        echo "Configuring Kura networking..."

        bash "${BASE_DIR}/${KURA_SYMLINK}/.data/manage_network_permissions.sh" -u
        restore_nm_installation
        flush_kernel_iptables
        reapply_user_iptables_backup
        recover_dnsmasq_conf_file
        remove_dnsmasq_leases

        recover_web_ui_kura_properties
        remove_kura_networking_service
        deregister_sibling
    fi

    # flush all cached filesystem to disk
    sync

    echo "Finished."
}

run_kura_networking_uninstall() {
    kura_uninstall &
    PID=$!
    START=$(date +%s)
}

run_kura_networking_uninstall

exit 0
