/*******************************************************************************
 * Copyright (c) 2023, 2026 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.nm.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.kura.net.status.modem.ModemPowerState;
import org.freedesktop.dbus.types.UInt32;
import org.junit.jupiter.api.Test;

public class MMModemPowerStateTest {

    @org.junit.jupiter.api.Nested
    public class MMModemPowerStateToMMModemPowerStateTest {

        public static Collection<Object[]> ModemPowerStateParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {new UInt32(0x00), MMModemPowerState.MM_MODEM_POWER_STATE_UNKNOWN});
            params.add(new Object[] {new UInt32(0x01), MMModemPowerState.MM_MODEM_POWER_STATE_OFF});
            params.add(new Object[] {new UInt32(0x02), MMModemPowerState.MM_MODEM_POWER_STATE_LOW});
            params.add(new Object[] {new UInt32(0x03), MMModemPowerState.MM_MODEM_POWER_STATE_ON});
            params.add(new Object[] {new UInt32(0x13), MMModemPowerState.MM_MODEM_POWER_STATE_UNKNOWN});
            return params;
        }

        private UInt32 inputIntValue;
        private MMModemPowerState expectedMMModemPowerState;
        private MMModemPowerState calculatedMMModemPowerState;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("ModemPowerStateParams")
        public void shouldReturnCorrectMMModemPowerState(UInt32 intValue, MMModemPowerState modemPowerState) {
            this.inputIntValue = intValue;
            this.expectedMMModemPowerState = modemPowerState;

            whenCalculateMMModemPowerState();
            thenCalculatedMMModemPowerStateIsCorrect();
        }

        private void whenCalculateMMModemPowerState() {
            this.calculatedMMModemPowerState = MMModemPowerState.toMMModemPowerState(this.inputIntValue);
        }

        private void thenCalculatedMMModemPowerStateIsCorrect() {
            assertEquals(this.expectedMMModemPowerState, this.calculatedMMModemPowerState);
        }
    }

    @org.junit.jupiter.api.Nested
    public class MMModemPowerStateToModemPowerStateTest {

        public static Collection<Object[]> ModemPowerStateParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {new UInt32(0x00), ModemPowerState.UNKNOWN});
            params.add(new Object[] {new UInt32(0x01), ModemPowerState.OFF});
            params.add(new Object[] {new UInt32(0x02), ModemPowerState.LOW});
            params.add(new Object[] {new UInt32(0x03), ModemPowerState.ON});
            params.add(new Object[] {new UInt32(0x13), ModemPowerState.UNKNOWN});
            return params;
        }

        private UInt32 inputIntValue;
        private ModemPowerState expectedModemPowerState;
        private ModemPowerState calculatedModemPowerState;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("ModemPowerStateParams")
        public void shouldReturnCorrectModemPowerState(UInt32 intValue, ModemPowerState modemPowerState) {
            this.inputIntValue = intValue;
            this.expectedModemPowerState = modemPowerState;

            whenCalculateModemPowerState();
            thenCalculatedModemPowerStateIsCorrect();
        }

        private void whenCalculateModemPowerState() {
            this.calculatedModemPowerState = MMModemPowerState.toModemPowerState(this.inputIntValue);
        }

        private void thenCalculatedModemPowerStateIsCorrect() {
            assertEquals(this.expectedModemPowerState, this.calculatedModemPowerState);
        }
    }

    @org.junit.jupiter.api.Nested
    public class MMModemPowerStateToUInt32Test {

        public static Collection<Object[]> ModemPowerStateParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {MMModemPowerState.MM_MODEM_POWER_STATE_UNKNOWN, new UInt32(0x00)});
            params.add(new Object[] {MMModemPowerState.MM_MODEM_POWER_STATE_OFF, new UInt32(0x01)});
            params.add(new Object[] {MMModemPowerState.MM_MODEM_POWER_STATE_LOW, new UInt32(0x02)});
            params.add(new Object[] {MMModemPowerState.MM_MODEM_POWER_STATE_ON, new UInt32(0x03)});
            return params;
        }

        private MMModemPowerState inputModemPowerState;
        private UInt32 expectedIntValue;
        private UInt32 calculatedUInt32;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("ModemPowerStateParams")
        public void shouldReturnCorrectUInt32(MMModemPowerState modemPowerState, UInt32 intValue) {
            this.expectedIntValue = intValue;
            this.inputModemPowerState = modemPowerState;

            whenCalculatedUInt32();
            thenCalculatedUInt32IsCorrect();
        }

        private void whenCalculatedUInt32() {
            this.calculatedUInt32 = this.inputModemPowerState.toUInt32();
        }

        private void thenCalculatedUInt32IsCorrect() {
            assertEquals(this.expectedIntValue, this.calculatedUInt32);
        }
    }
}
