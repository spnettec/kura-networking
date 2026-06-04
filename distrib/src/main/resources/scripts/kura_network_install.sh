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

error_state() {
    echo ""
    touch /tmp/install_error
    exit 1
}

# Register this sibling in the kura-core install-order registry, identically to
# every other sibling (wires, opcua, ...). Idempotent. kura-networking installs
# under siblings/networking/, so it registers the name "networking" (the
# mutually-exclusive kura-firewall-only registers "firewall").
register_sibling() {
    SIBLING_NAME="networking"
    REGISTRY="${BASE_DIR}/${KURA_SYMLINK}/framework/sibling-install-order"
    if [ -f "${REGISTRY}" ]; then
        if ! grep -qFx "${SIBLING_NAME}" "${REGISTRY}"; then
            echo "  Registering ${SIBLING_NAME} in sibling-install-order"
            echo "${SIBLING_NAME}" >> "${REGISTRY}"
        else
            echo "  ${SIBLING_NAME} already registered in sibling-install-order"
        fi
    else
        echo "  Creating sibling-install-order registry"
        mkdir -p "${BASE_DIR}/${KURA_SYMLINK}/framework" 2>/dev/null || true
        echo "${SIBLING_NAME}" > "${REGISTRY}"
        chown kurad:kurad "${REGISTRY}" 2>/dev/null || true
        chmod 664 "${REGISTRY}" 2>/dev/null || true
    fi
}

systemctl_if_present() {
    ACTION="$1"
    SERVICE="$2"

    if systemctl list-unit-files "${SERVICE}.service" > /dev/null 2>&1 || systemctl list-units --all "${SERVICE}.service" > /dev/null 2>&1; then
        systemctl "${ACTION}" "${SERVICE}" > /dev/null 2>&1 || true
    fi
}

backup_files() {
    SUFFIX="${1}"

    shift

    for file in "${@}"
    do
        if [ -f "${file}" ]; then
            mv "${file}" "${file}.${SUFFIX}"
        fi
    done
}

install_named_config() {
    echo "Installing configuration for named."

    if [ ! -d "/var/named" ]; then
        mkdir /var/named
    fi

    install --backup --suffix=.kurasave --mode=644 --owner=bind "${BASE_DIR}/${KURA_SYMLINK}/.data/named.ca" /var/named/named.ca
    install --backup --suffix=.kurasave --mode=644 --owner=bind "${BASE_DIR}/${KURA_SYMLINK}/.data/named.rfc1912.zones" /etc/named.rfc1912.zones

    if [ -d "/etc/apparmor.d" ]; then
        install --mode=644 --owner=bind "${BASE_DIR}/${KURA_SYMLINK}/.data/usr.sbin.named" /etc/apparmor.d/usr.sbin.named
    fi
    
    if [ ! -f "/etc/bind/rndc.key" ] ; then
        rndc-confgen -r /dev/urandom -a
    fi
    chown bind:bind /etc/bind/rndc.key
    chmod 600 /etc/bind/rndc.key

    if [ -f "/etc/bind/named.conf" ] ; then
        cp /etc/bind/named.conf /etc/bind/named.conf.kurasave
        chmod a+r /etc/bind/named.conf
    fi
    
    chown -R bind /var/named

    echo "named installed."
}

disable_netplan() {
    # Override netplan renderer to NetworkManager via highest-priority drop-in.
    # Existing yamls are kept so that interface configs (e.g. eth0 dhcp4) are
    # inherited by NM through netplan merge — otherwise the gateway would lose
    # IPv4 connectivity between install and the first Kura UI configuration.
    if [ -d /etc/netplan  ]; then
        cat > /etc/netplan/zz-kura-use-nm.yaml <<EOF
network:
  version: 2
  renderer: NetworkManager
EOF
        chmod 600 /etc/netplan/zz-kura-use-nm.yaml
    fi
}

setup_network_manager() {
    systemctl daemon-reload
    systemctl_if_present disable firewalld
    systemctl_if_present disable iptables
    systemctl_if_present disable ip6tables
    systemctl_if_present enable firewall
    systemctl_if_present enable NetworkManager
    systemctl_if_present enable ModemManager
    systemctl_if_present stop dnsmasq
    systemctl_if_present disable dnsmasq
    systemctl_if_present stop isc-dhcp-server
    systemctl_if_present disable isc-dhcp-server
    systemctl_if_present stop isc-dhcp-server6
    systemctl_if_present disable isc-dhcp-server6
    systemctl_if_present stop hostapd
    systemctl_if_present disable hostapd
    systemctl_if_present stop dhcpcd
    systemctl_if_present disable dhcpcd
    systemctl_if_present stop dhcpcd5
    systemctl_if_present disable dhcpcd5
    systemctl_if_present stop systemd-networkd
    systemctl_if_present disable systemd-networkd

    # setup iptables
    if [ ! -d /etc/sysconfig ]; then
        mkdir /etc/sysconfig
    fi

    # Back up the existing iptables file as .kurasave so uninstall can restore
    # the pre-Kura state. But never back up a file that is itself a Kura rule
    # dump (e.g. left behind by an older buggy uninstall): doing so chains Kura
    # content through repeated install/uninstall cycles and resurrects the
    # input-kura / forward-kura / postrouting-kura chains every time.
    if test -f /etc/sysconfig/iptables; then
        if grep -qE '^:?(input-kura|forward-kura|output-kura|prerouting-kura|postrouting-kura)' /etc/sysconfig/iptables; then
            rm -f /etc/sysconfig/iptables
        elif test -f /etc/sysconfig/iptables.kurasave; then
            # An earlier genuine backup is already saved; don't overwrite it
            # with whatever state we're seeing now.
            rm -f /etc/sysconfig/iptables
        else
            mv /etc/sysconfig/iptables /etc/sysconfig/iptables.kurasave
        fi
    fi
    sed -i "s|KURA_DIR|${BASE_DIR}/${KURA_SYMLINK}|" /lib/systemd/system/firewall.service
    cp -p /proc/sys/net/ipv4/ip_forward "${BASE_DIR}/${KURA_SYMLINK}/.data/ip_forward.kurasave"

    # disables cloud-init if exists and allows interface management to network-manager
    if [ -d /etc/cloud/cloud.cfg.d ]; then
        echo "network: {config: disabled}" | sudo tee -a /etc/cloud/cloud.cfg.d/99-disable-network-config.cfg > /dev/null
    fi
    if [ -d /usr/lib/NetworkManager/conf.d/ ]; then
        TO_REMOVE=$( find /usr/lib/NetworkManager/conf.d/ -type f -name  "*-globally-managed-devices.conf" | awk 'NR==1{print $1}' )
        
        if [ -f "${TO_REMOVE}" ]; then
            rm "${TO_REMOVE}"
        fi
    fi
    # comment network interface configurations in interfaces file
    if python3 -V > /dev/null 2>&1
    then
        python3 "${BASE_DIR}/${KURA_SYMLINK}/kura-networking-install/comment_interfaces_file.py"
    else
        echo "python3 not found. Please manually review the /etc/network/interfaces file and comment configured network interfaces."
    fi
    
    # install dnsmasq default configuration
    if [ -f /etc/default/dnsmasq ]; then
        mv /etc/default/dnsmasq /etc/default/dnsmasq.kurasave
    fi
    cp "${BASE_DIR}/${KURA_SYMLINK}/kura-networking-install/dnsmasq" /etc/default/dnsmasq

    install_named_config

    disable_netplan

    # Bring NetworkManager up immediately so the gateway keeps IPv4 between
    # install and reboot — relying solely on reboot has bitten users on
    # cloud/VM installs where reconnecting requires SSH.
    systemctl start NetworkManager > /dev/null 2>&1
    if command -v netplan > /dev/null 2>&1; then
        netplan apply > /dev/null 2>&1
    fi
}

setup_dnsmasq_conf_file() {
    
    DNSMASQ_CONF_FILE="/etc/dnsmasq.conf"
    if [ -f "${DNSMASQ_CONF_FILE}" ]; then
        if [ ! -f "${DNSMASQ_CONF_FILE}.kurasave" ]; then
            cp -f "${DNSMASQ_CONF_FILE}" "${DNSMASQ_CONF_FILE}.kurasave"
        fi
        sed -i s/^dhcp-/#dhcp-/g "${DNSMASQ_CONF_FILE}"
    fi

    DNSMASQ_LXC_FILE="/etc/dnsmasq.d/lxc"
    if [ -f "${DNSMASQ_LXC_FILE}" ]; then
        if [ ! -f "/etc/lxc.kurasave" ]; then
            cp -f "${DNSMASQ_LXC_FILE}" "/etc/lxc.kurasave"
        fi
        sed -i s/^bind-interfaces/bind-dynamic/g "${DNSMASQ_LXC_FILE}"
    fi
}

stop_and_disable_services() {
    for service in ${SERVICES_TO_STOP_AND_DISABLE}; do
        echo "Stopping and disabling $service."
        systemctl stop "${service}" &> /dev/null
        systemctl disable "${service}" &> /dev/null
    done
}

should_disable_systemd_resolved_stub() {
    if [ -x "$(command -v systemd)" ]; then

        SYSTEMD_VERSION=$(systemd --version | (IFS=" " read -r _ignore SYSTEMD_VERSION _ignore; echo "${SYSTEMD_VERSION}") || true)

        [ "${SYSTEMD_VERSION}" -lt 248 ] && 
        [ -L /etc/resolv.conf ] && 
        grep "^nameserver[ ]\+127.0.0.53" < /etc/resolv.conf > /dev/null 2>&1 && 
        [ -e /run/systemd/resolve/resolv.conf ]
    else
        false
    fi
}

setup_web_ui_kura_properties() {
    if [ -f "${BASE_DIR}/${KURA_SYMLINK}/framework/kura.properties" ]; then
        set_kura_property "kura.have.net.admin" "true"
        set_kura_property "kura.have.firewall.admin" "true"
    fi
}

set_kura_property() {
    KEY="$1"
    VALUE="$2"
    PROPERTIES_FILE="${BASE_DIR}/${KURA_SYMLINK}/framework/kura.properties"

    if grep -q "^#*${KEY}=" "${PROPERTIES_FILE}"; then
        sed -i "s|^#*${KEY}=.*|${KEY}=${VALUE}|" "${PROPERTIES_FILE}"
    else
        echo "${KEY}=${VALUE}" >> "${PROPERTIES_FILE}"
    fi
}

kura_install() {
    echo "Installing Kura networking..."

    if [ ! -d "/usr/lib/systemd/system/kura.service.d" ]; then
      mkdir -p "/usr/lib/systemd/system/kura.service.d"
    fi

    cp "${BASE_DIR}/${KURA_SYMLINK}/kura-networking-install/kura-networking.conf" /usr/lib/systemd/system/kura.service.d/kura-networking.conf
    systemctl daemon-reload
    systemctl enable kura

    stop_and_disable_services

    echo "Configuring Kura networking..."

    chmod 700 "${BASE_DIR}/${KURA_SYMLINK}/.data/manage_network_permissions.sh"

    if ! ( "${BASE_DIR}/${KURA_SYMLINK}/.data/manage_network_permissions.sh" -i ); then
        error_state "Error installing network permissions."
    fi

    setup_network_manager

    bash "${BASE_DIR}/${KURA_SYMLINK}/kura-networking-install/customize-installation.sh" "install" true

    setup_web_ui_kura_properties


    if should_disable_systemd_resolved_stub; then
        ln -sf /run/systemd/resolve/resolv.conf /etc/resolv.conf
    fi

    setup_dnsmasq_conf_file

    rm -r "${BASE_DIR}/${KURA_SYMLINK}/kura-networking-install"

    # flush all cached filesystem to disk
    sync

    echo "Finished."
}

run_kura_network_install() {
    set -m
    kura_install &
    set +m
}

SYSTEM=$(ls -all /sbin/init);
if  ! echo "${SYSTEM}" | grep -qe "systemd" ; then
    error_state "Incompatible system version ${SYSTEM##* }"
fi

# check if java or keytool are not found
if (! java -version > /dev/null 2>&1) || (! keytool > /dev/null 2>&1); then
    # check if JVM path is not set
    if [ -z "${JAVA_HOME}" ]; then
        # retrieve java home using the link
        JAVA_HOME=$(readlink $(which java) | rev | cut -d"/" -f3- | rev)
    fi

    if [ -n "${JAVA_HOME}" ]; then
        # create a script that exports the variables at startup in /etc/profile.d
        touch /etc/profile.d/java_paths.sh
        echo "export JAVA_HOME=$JAVA_HOME" > /etc/profile.d/java_paths.sh

        # check if java bin already exists in PATH
        if [[ ":$PATH:" != *":$JAVA_HOME/bin:"* ]]; then
            echo "export PATH=$PATH:$JAVA_HOME/bin" >> /etc/profile.d/java_paths.sh
        fi

        # export the variables just for this session 
        source /etc/profile.d/java_paths.sh
    else
        error_state "Java binary cannot be found."
    fi
fi

register_sibling

run_kura_network_install

exit 0
