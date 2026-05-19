/*******************************************************************************
 * Copyright (c) 2011, 2023 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.linux.net.iptables;

import java.io.File;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.executor.CommandExecutorService;
import org.eclipse.kura.net.IP4Address;
import org.eclipse.kura.net.IPAddress;
import org.eclipse.kura.net.NetworkPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Linux firewall implementation
 *
 * @author eurotech
 */
public class LinuxFirewall extends AbstractLinuxFirewall {

    private static final Logger logger = LoggerFactory.getLogger(LinuxFirewall.class);

    private static LinuxFirewall linuxFirewall;
    private static final String IP_FORWARD_FILE_NAME = "/proc/sys/net/ipv4/ip_forward";

    // The LinuxFirewall is a singleton, since at the creation of the object
    // it reads the iptables configuration from the filesystem once, instead of
    // reading it at every call of the constructor.
    // However, it is dangerous in a multi-thread system because the CommandExecutorService
    // is passed as an argument.
    public static LinuxFirewall getInstance(CommandExecutorService executorService) {
        if (linuxFirewall == null) {
            linuxFirewall = new LinuxFirewall(executorService);
        } else {
            linuxFirewall.setExecutorService(executorService);
        }
        return linuxFirewall;
    }

    private LinuxFirewall(CommandExecutorService executorService) {
        this.executorService = executorService;
        this.iptables = new IptablesConfig(this.executorService);
        try {
            File cfgFile = new File(this.iptables.getFirewallConfigFileName());
            if (!cfgFile.exists()) {
                IptablesConfig minimalConfig = new IptablesConfig(executorService);
                seedSafetyRules(minimalConfig);
                minimalConfig.applyRules();
            }
            initialize();
        } catch (KuraException e) {
            logger.error("failed to initialize LinuxFirewall", e);
        }
    }

    // First boot has no /etc/sysconfig/iptables, so applyRules() runs against
    // an empty config: INPUT DROP + empty input-kura → SSH + Web are dead until
    // FirewallConfigurationService.activate() pushes the snapshot's open ports.
    // If snapshot loading is broken (corrupt file, missing entry, encrypt
    // mismatch), the gateway is wedged. Seed 22 + 443 here so there is always
    // a way back in. Metatype default (DFLT_IPV4_OPEN_PORTS_VALUE) handles the
    // ConfigAdmin path; this handles the kernel path.
    private void seedSafetyRules(IptablesConfig minimalConfig) {
        try {
            Set<LocalRule> seed = new LinkedHashSet<>();
            NetworkPair<IPAddress> anywhere = new NetworkPair<>(IP4Address.getDefaultAddress(), (short) 0);
            seed.add(new LocalRule(22, "tcp", anywhere, null, null, null, null));
            seed.add(new LocalRule(443, "tcp", anywhere, null, null, null, null));
            minimalConfig.setLocalRules(seed);
            logger.info("Seeded minimal iptables config with SSH (22) + HTTPS (443) safety rules");
        } catch (UnknownHostException e) {
            logger.warn("Failed to seed safety rules; first-boot INPUT chain will be deny-all", e);
        }
    }

    @Override
    protected IPAddress getDefaultAddress() throws UnknownHostException {
        return IP4Address.getDefaultAddress();
    }

    @Override
    protected String getIpForwardFileName() {
        return IP_FORWARD_FILE_NAME;
    }
}
