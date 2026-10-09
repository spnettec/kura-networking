/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.kura.linux.net.iptables;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.kura.net.IPAddress;
import org.eclipse.kura.net.firewall.RuleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FirewallBlockAllPortsTest {
    @TempDir
    Path directory;

    @Test
    void blockingPortsAlsoRemovesManualNatRules() throws Exception {
        IptablesConfig config = mock(IptablesConfig.class);
        AbstractLinuxFirewall firewall = new AbstractLinuxFirewall() {
            @Override protected IPAddress getDefaultAddress() throws UnknownHostException {
                return IPAddress.parseHostAddress("0.0.0.0");
            }
            @Override protected String getIpForwardFileName() {
                return directory.resolve("ip_forward").toString();
            }
        };
        firewall.iptables = config;
        firewall.addNatRules(List.of(new NATRule("eth0", "eth1", null, null, null, true, RuleType.GENERIC)));

        firewall.blockAllPorts();

        assertTrue(firewall.getNatRules().isEmpty(), "Manual NAT must not keep forwarding after blockAllPorts");
        verify(config, atLeastOnce()).setNatRules(argThat(java.util.Set::isEmpty));
    }
}
