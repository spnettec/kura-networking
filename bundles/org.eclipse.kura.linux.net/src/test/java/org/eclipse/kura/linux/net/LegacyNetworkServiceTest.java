/*******************************************************************************
 * Copyright (c) 2011, 2026 Eurotech and/or its affiliates and others
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.kura.linux.net;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.eclipse.kura.linux.net.util.LinuxIfconfig;
import org.eclipse.kura.linux.net.util.LinuxNetworkUtil;
import org.eclipse.kura.net.NetInterface;
import org.eclipse.kura.net.NetInterfaceAddress;
import org.eclipse.kura.net.NetInterfaceType;
import org.eclipse.kura.net.NetworkService;
import org.eclipse.kura.usb.UsbService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.MockedStatic;
import org.osgi.service.event.EventAdmin;

/** Legacy core enumeration assertions at the LinuxNetworkUtil component boundary, without host D-Bus. */
@Timeout(10)
class LegacyNetworkServiceTest {
    private NetworkServiceImpl service;
    private MockedStatic<LinuxNetworkUtil> staticNetwork;

    @BeforeEach
    void prepareInterfaces() throws Exception {
        staticNetwork = mockStatic(LinuxNetworkUtil.class);
        LinuxNetworkUtil linux = mock(LinuxNetworkUtil.class);
        service = new NetworkServiceImpl();
        service.setLinuxNetworkUtil(linux);
        service.setUsbService(mock(UsbService.class));
        service.setEventAdmin(mock(EventAdmin.class));
        when(linux.getAllInterfaceNames()).thenReturn(List.of("eth0", "eth1", "lo"));
        for (String name : List.of("eth0", "eth1", "lo")) {
            var configuration = new LinuxIfconfig(name);
            configuration.setType(name.equals("lo") ? NetInterfaceType.LOOPBACK : NetInterfaceType.ETHERNET);
            configuration.setUp(true);
            when(linux.getInterfaceConfiguration(name)).thenReturn(configuration);
            when(linux.getEthernetDriver(name)).thenReturn(Map.of("name", "fixture-driver", "version", "1", "firmware", "1"));
            when(linux.hasAddress(name)).thenReturn(!name.equals("eth1"));
        }
    }

    @AfterEach
    void releaseInterfaces() {
        try { if (service != null) { service.deactivate(null); } }
        finally { if (staticNetwork != null) { staticNetwork.close(); } }
    }

    @Test
    void testServiceExists() {
        // Component/API presence only. Actual SCR service availability remains a runtime acceptance check.
        assertInstanceOf(NetworkService.class, service);
    }

    @Test
    void testInterfaceNamesList() throws Exception {
        List<String> names = service.getAllNetworkInterfaceNames();
        assertEquals(List.of("eth0", "eth1", "lo"), names);
        assertTrue(names.stream().noneMatch(String::isEmpty));
    }

    @Test
    void testAllInterfaces() throws Exception {
        var interfaces = service.getNetworkInterfaces();
        assertEquals(List.of("eth0", "eth1", "lo"), interfaces.stream().map(NetInterface::getName).toList());
        assertInterfaceMetadata(interfaces);
    }

    @Test
    void testActiveInterfaces() throws Exception {
        var interfaces = service.getActiveNetworkInterfaces();
        // Current Linux implementation defines active by hasAddress, not merely the link/up flag.
        assertEquals(List.of("eth0", "lo"), interfaces.stream().map(NetInterface::getName).toList());
        assertInterfaceMetadata(interfaces);
    }

    private static void assertInterfaceMetadata(List<NetInterface<? extends NetInterfaceAddress>> interfaces) {
        assertFalse(interfaces.isEmpty());
        for (var network : interfaces) {
            assertNotNull(network.getName());
            assertFalse(network.getName().isEmpty());
            assertNotNull(network.getType());
            assertNotNull(network.getState());
            assertNotNull(network.getNetInterfaceAddresses());
        }
    }
}
