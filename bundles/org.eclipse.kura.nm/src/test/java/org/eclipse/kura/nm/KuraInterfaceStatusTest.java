/*******************************************************************************
 * Copyright (c) 2025, 2026 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.nm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class KuraInterfaceStatusTest {

    public KuraIpStatus ip4Status;

    public KuraIpStatus ip6Status;

    public KuraInterfaceStatus expectedResult;

    public Class<? extends Exception> expectedException;

public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { //
            {KuraIpStatus.DISABLED, KuraIpStatus.DISABLED, KuraInterfaceStatus.DISABLED, null}, //
            {KuraIpStatus.ENABLEDLAN, KuraIpStatus.ENABLEDLAN, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.ENABLEDWAN, KuraIpStatus.ENABLEDWAN, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.ENABLEDLAN, KuraIpStatus.ENABLEDWAN, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.ENABLEDWAN, KuraIpStatus.ENABLEDLAN, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.ENABLEDLAN, KuraIpStatus.DISABLED, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.ENABLEDWAN, KuraIpStatus.DISABLED, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.DISABLED, KuraIpStatus.ENABLEDLAN, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.DISABLED, KuraIpStatus.ENABLEDWAN, KuraInterfaceStatus.ENABLED, null}, //
            {KuraIpStatus.L2ONLY, KuraIpStatus.DISABLED, KuraInterfaceStatus.UNMANAGED, null}, //
            {KuraIpStatus.L2ONLY, KuraIpStatus.ENABLEDLAN, KuraInterfaceStatus.UNMANAGED, null}, //
            {KuraIpStatus.L2ONLY, KuraIpStatus.ENABLEDWAN, KuraInterfaceStatus.UNMANAGED, null}, //
            {KuraIpStatus.L2ONLY, KuraIpStatus.L2ONLY, KuraInterfaceStatus.UNMANAGED, null}, //
            {KuraIpStatus.UNMANAGED, KuraIpStatus.UNMANAGED, KuraInterfaceStatus.UNMANAGED, null}, //
            // The fork permits unmanaged IPv4 when IPv6 is disabled.
            {KuraIpStatus.UNMANAGED, KuraIpStatus.DISABLED, KuraInterfaceStatus.UNMANAGED, null}, //
            {KuraIpStatus.UNMANAGED, KuraIpStatus.ENABLEDLAN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNMANAGED, KuraIpStatus.ENABLEDWAN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNMANAGED, KuraIpStatus.L2ONLY, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNKNOWN, KuraIpStatus.DISABLED, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNKNOWN, KuraIpStatus.ENABLEDLAN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNKNOWN, KuraIpStatus.ENABLEDWAN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNKNOWN, KuraIpStatus.L2ONLY, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNKNOWN, KuraIpStatus.UNMANAGED, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNKNOWN, KuraIpStatus.UNKNOWN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.DISABLED, KuraIpStatus.UNKNOWN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.ENABLEDLAN, KuraIpStatus.UNKNOWN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.ENABLEDWAN, KuraIpStatus.UNKNOWN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.UNMANAGED, KuraIpStatus.UNKNOWN, null, IllegalArgumentException.class}, //
            {KuraIpStatus.L2ONLY, KuraIpStatus.UNKNOWN, null, IllegalArgumentException.class}, //
        });
    }

    @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("data")
        public void test(KuraIpStatus ip4Status, KuraIpStatus ip6Status, KuraInterfaceStatus expectedResult, Class<? extends Exception> expectedException) throws IllegalArgumentException {
            this.ip4Status = ip4Status;
            this.ip6Status = ip6Status;
            this.expectedResult = expectedResult;
            this.expectedException = expectedException;
        if (expectedException != null) {
            org.junit.jupiter.api.Assertions.assertThrows(expectedException,
                    () -> KuraInterfaceStatus.fromKuraIpStatus(ip4Status, ip6Status));
        } else {
            assertEquals(expectedResult, KuraInterfaceStatus.fromKuraIpStatus(ip4Status, ip6Status));
        }
    }
}
