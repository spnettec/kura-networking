#!/usr/bin/env python3
#
# Copyright (c) 2026 Eurotech and/or its affiliates and others
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

import logging
import sys

FIREWALL_PID = "org.eclipse.kura.net.admin.FirewallConfigurationService"
CLOSING_TAG = "</esf:configurations>"

FIREWALL_CONFIGURATION = """    <esf:configuration pid="org.eclipse.kura.net.admin.FirewallConfigurationService">
        <esf:properties>
            <esf:property array="false" encrypted="false" name="firewall.open.ports" type="String">
                <esf:value>22,tcp,,,,,,#;443,tcp,,,,,,#;4443,tcp,,,,,,#;5002,tcp,127.0.0.1/32,,,,,#;8000,tcp,,,,,,#;</esf:value>
            </esf:property>
            <esf:property array="false" encrypted="false" name="firewall.port.forwarding" type="String">
                <esf:value></esf:value>
            </esf:property>
            <esf:property array="false" encrypted="false" name="firewall.nat" type="String">
                <esf:value></esf:value>
            </esf:property>
        </esf:properties>
    </esf:configuration>
"""


def main(snapshot_path):
    logging.basicConfig(format="[customize_firewall_snapshot.py] %(levelname)s %(message)s", level=logging.INFO)

    with open(snapshot_path, "r", encoding="utf-8") as snapshot:
        snapshot_content = snapshot.read()

    if FIREWALL_PID in snapshot_content:
        logging.info("%s already contains firewall configuration", snapshot_path)
        return

    if CLOSING_TAG not in snapshot_content:
        raise ValueError(f"{snapshot_path} does not contain {CLOSING_TAG}")

    snapshot_content = snapshot_content.replace(CLOSING_TAG, FIREWALL_CONFIGURATION + CLOSING_TAG)

    with open(snapshot_path, "w", encoding="utf-8") as snapshot:
        snapshot.write(snapshot_content)

    logging.info("%s: added firewall-only configuration", snapshot_path)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Usage: customize_firewall_snapshot.py <snapshot_0.xml>")
    main(sys.argv[1])
