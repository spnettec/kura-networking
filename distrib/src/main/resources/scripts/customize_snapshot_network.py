#!/usr/bin/env python3
#
# Copyright (c) 2024, 2025 Eurotech and/or its affiliates
#
#  All rights reserved.
#
import sys
import logging
import os
import os.path
import argparse
import glob

sys.path.append("/opt/eclipse/kura/kura-networking-install")

from network_tools import get_eth_wlan_interfaces_names

# Define constants at the top of the file
TEMPLATE_DIR = "/opt/eclipse/kura/kura-networking-install"
TEMPLATES = {
    "firewall_eth": os.path.join(TEMPLATE_DIR, "template_firewall_eth"),
    "firewall_wlan" : os.path.join(TEMPLATE_DIR, "template_firewall_eth_wlan"),
    "flooding": os.path.join(TEMPLATE_DIR, "template_flooding"),
    "multiple_eth_no_wlan" : os.path.join(TEMPLATE_DIR, "template_multiple_eth_no_wlan"),
    "multiple_eth_one_wlan" : os.path.join(TEMPLATE_DIR, "template_multiple_eth_one_wlan"),
    "one_eth_no_wlan" : os.path.join(TEMPLATE_DIR, "template_one_eth_no_wlan"),
    "one_eth_one_wlan" : os.path.join(TEMPLATE_DIR, "template_one_eth_one_wlan")
}

SNAPSHOT_DIR = "/opt/eclipse/kura/user/snapshots"
DATA_SNAPSHOT_DIR = "/opt/eclipse/kura/.data"

def validate_file_path(file_path):
    abs_path = os.path.abspath(file_path)
    allowed_dirs = [
        "/opt/eclipse/kura/user/snapshots/",
        "/opt/eclipse/kura/.data/"
    ]
    for allowed_dir in allowed_dirs:
        if abs_path.startswith(allowed_dir):
            return abs_path
    raise ValueError("File path not allowed: {}".format(abs_path))

def read_file_safely(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()
    except (IOError, PermissionError) as e:
        logging.error("Error reading file %s: %s", file_path, str(e))
        raise

def write_file_safely(file_path, content):
    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
    except (IOError, PermissionError) as e:
        logging.error("Error writing file %s: %s", file_path, str(e))
        raise

def validate_templates():
    for name, path in TEMPLATES.items():
        if not os.path.isfile(path):
            logging.error("Template file missing: %s", path)
            return False
    return True

def merge_config_blocks(snapshot_path, inserted_content):
    """Merge pre-built config blocks into a single snapshot file. Returns True if changed."""
    snapshot_content = read_file_safely(snapshot_path)

    network_present = "NetworkConfigurationService" in snapshot_content
    firewall_present = "FirewallConfigurationService" in snapshot_content
    flooding_present = "FloodingProtectionConfigurator" in snapshot_content

    if network_present and firewall_present and flooding_present:
        logging.info("%s : all configs already present, skipping", snapshot_path)
        return False

    to_insert = ""
    if not network_present:
        to_insert += inserted_content["network"] + "\n"
        logging.info("%s : adding network configuration", snapshot_path)
    if not firewall_present:
        to_insert += inserted_content["firewall"] + "\n"
        logging.info("%s : adding firewall configuration", snapshot_path)
    if not flooding_present:
        to_insert += inserted_content["flooding"] + "\n"
        logging.info("%s : adding flooding protection configuration", snapshot_path)

    closing_tag = '</esf:configurations>'
    insert_position = snapshot_content.rfind(closing_tag)
    snapshot_content = snapshot_content[:insert_position] + to_insert + snapshot_content[insert_position:]

    write_file_safely(snapshot_path, snapshot_content)
    logging.info("%s : successfully edited", snapshot_path)
    return True

def main():
    logging.basicConfig(
        format='[customize_snapshot_network.py] %(asctime)s %(levelname)s %(message)s',
        level=logging.INFO,
        datefmt='%Y-%m-%d %H:%M:%S',
        handlers=[
            logging.StreamHandler()
        ]
    )

    parser = argparse.ArgumentParser(description="Customize snapshot_0.xml file", usage='%(prog)s snapshot_filename')
    parser.add_argument('snapshot_filename', help='The path of the snapshot_0.xml file')
    args = parser.parse_args()

    try:
        safe_path = validate_file_path(args.snapshot_filename)
    except ValueError as e:
        logging.error("Invalid file path: %s", str(e))
        sys.exit(1)

    (eth_names, wlan_names) = get_eth_wlan_interfaces_names()

    eth_number = len(eth_names)
    wlan_number = len(wlan_names)

    if eth_number == 0:
        logging.info("ERROR: no ethernet interface found")
        exit(1)

    if not validate_templates():
        logging.error("Template validation failed")
        exit(1)

    firewall_configuration_template = TEMPLATES["firewall_eth"]
    network_configuration_template = TEMPLATES["one_eth_no_wlan"]
    flooding_configuration_template = TEMPLATES["flooding"]

    if eth_number == 1:
        if wlan_number == 0:
            network_configuration_template = TEMPLATES["one_eth_no_wlan"]
            firewall_configuration_template = TEMPLATES["firewall_eth"]
        else:
            network_configuration_template = TEMPLATES["one_eth_one_wlan"]
            firewall_configuration_template = TEMPLATES["firewall_wlan"]
    else:
        if wlan_number == 0:
            network_configuration_template = TEMPLATES["multiple_eth_no_wlan"]
            firewall_configuration_template = TEMPLATES["firewall_eth"]
        else:
            network_configuration_template = TEMPLATES["multiple_eth_one_wlan"]
            firewall_configuration_template = TEMPLATES["firewall_wlan"]

    # Read templates
    network_template_content = read_file_safely(network_configuration_template)
    firewall_template_content = read_file_safely(firewall_configuration_template)
    flooding_template_content = read_file_safely(flooding_configuration_template)

    # Build interface list
    interfaces_list = "lo"
    for eth_name in eth_names[:2]:
        interfaces_list += "," + eth_name
    for wlan_name in wlan_names[:1]:
        interfaces_list += "," + wlan_name

    # Substitute interface names in templates (templates only; snapshot_0.xml
    # existing content also gets substituted below during its dedicated processing)
    for i, eth_name in enumerate(eth_names[:2]):
        network_template_content = network_template_content.replace('ETH_INTERFACE_' + str(i), eth_name)
        firewall_template_content = firewall_template_content.replace('ETH_INTERFACE_' + str(i), eth_name)
        flooding_template_content = flooding_template_content.replace('ETH_INTERFACE_' + str(i), eth_name)

    for i, wlan_name in enumerate(wlan_names[:1]):
        network_template_content = network_template_content.replace('WIFI_INTERFACE_' + str(i), wlan_name)
        firewall_template_content = firewall_template_content.replace('WIFI_INTERFACE_' + str(i), wlan_name)
        flooding_template_content = flooding_template_content.replace('WIFI_INTERFACE_' + str(i), wlan_name)

    network_template_content = network_template_content.replace('INTERFACES_LIST', interfaces_list)
    firewall_template_content = firewall_template_content.replace('INTERFACES_LIST', interfaces_list)
    flooding_template_content = flooding_template_content.replace('INTERFACES_LIST', interfaces_list)

    inserted_content = {
        "network": network_template_content,
        "firewall": firewall_template_content,
        "flooding": flooding_template_content,
    }

    # ---- 1. Process snapshot_0.xml (primary target) ----
    logging.info("%s : starting editing", safe_path)
    merge_config_blocks(safe_path, inserted_content)

    # ---- 2. Also merge into all timestamped snapshots ----
    snapshot_pattern = os.path.join(SNAPSHOT_DIR, "snapshot_*.xml")
    for snap_file in sorted(glob.glob(snapshot_pattern)):
        if os.path.basename(snap_file) == os.path.basename(safe_path):
            continue  # already processed
        logging.info("%s : found additional snapshot", snap_file)
        merge_config_blocks(snap_file, inserted_content)

    # ---- 3. Also merge into .data/snapshot_0.xml ----
    data_snapshot = os.path.join(DATA_SNAPSHOT_DIR, "snapshot_0.xml")
    if os.path.isfile(data_snapshot) and os.path.abspath(data_snapshot) != os.path.abspath(safe_path):
        logging.info("%s : found data snapshot", data_snapshot)
        merge_config_blocks(data_snapshot, inserted_content)

if __name__ == "__main__":
    main()
