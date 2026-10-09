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
import org.eclipse.kura.net.status.modem.SimType;
import org.freedesktop.dbus.types.UInt32;
import org.junit.jupiter.api.Test;

public class MMSimTypeTest {

    @org.junit.jupiter.api.Nested
    public class MMSimTypeToMMSimTypeTest {

        public static Collection<Object[]> SimTypeParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {new UInt32(0x00), MMSimType.MM_SIM_TYPE_UNKNOWN});
            params.add(new Object[] {new UInt32(0x01), MMSimType.MM_SIM_TYPE_PHYSICAL});
            params.add(new Object[] {new UInt32(0x02), MMSimType.MM_SIM_TYPE_ESIM});
            params.add(new Object[] {new UInt32(0x14), MMSimType.MM_SIM_TYPE_UNKNOWN});
            return params;
        }

        private UInt32 inputIntValue;
        private MMSimType expectedSimType;
        private MMSimType calculatedSimType;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("SimTypeParams")
        public void shouldReturnCorrectMMSImType(UInt32 intValue, MMSimType simType) {
            this.inputIntValue = intValue;
            this.expectedSimType = simType;

            whenCalculateMMSimType();
            thenCalculatedMMSimTypeIsCorrect();
        }

        private void whenCalculateMMSimType() {
            this.calculatedSimType = MMSimType.toMMSimType(this.inputIntValue);
        }

        private void thenCalculatedMMSimTypeIsCorrect() {
            assertEquals(this.expectedSimType, this.calculatedSimType);
        }
    }

    @org.junit.jupiter.api.Nested
    public class MMSimTypeToSimTypeTest {

        public static Collection<Object[]> SimTypeParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {new UInt32(0x00), SimType.UNKNOWN});
            params.add(new Object[] {new UInt32(0x01), SimType.PHYSICAL});
            params.add(new Object[] {new UInt32(0x02), SimType.ESIM});
            params.add(new Object[] {new UInt32(0x14), SimType.UNKNOWN});
            return params;
        }

        private UInt32 inputIntValue;
        private SimType expectedSimType;
        private SimType calculatedSimType;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("SimTypeParams")
        public void shouldReturnCorrectSimType(UInt32 intValue, SimType simType) {
            this.inputIntValue = intValue;
            this.expectedSimType = simType;

            whenCalculatedSimType();
            thenCalculatedSimTypeIsCorrect();
        }

        private void whenCalculatedSimType() {
            this.calculatedSimType = MMSimType.toSimType(this.inputIntValue);
        }

        private void thenCalculatedSimTypeIsCorrect() {
            assertEquals(this.expectedSimType, this.calculatedSimType);
        }
    }

    @org.junit.jupiter.api.Nested
    public class MMSimTypeToUInt32Test {

        public static Collection<Object[]> SimTypeParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {
                MMSimType.MM_SIM_TYPE_UNKNOWN, new UInt32(0x00),
            });
            params.add(new Object[] {MMSimType.MM_SIM_TYPE_PHYSICAL, new UInt32(0x01)});
            params.add(new Object[] {MMSimType.MM_SIM_TYPE_ESIM, new UInt32(0x02)});
            return params;
        }

        private MMSimType inputSimType;
        private UInt32 expectedIntValue;
        private UInt32 calculatedUInt32;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("SimTypeParams")
        public void shouldReturnCorrectUInt32(MMSimType simType, UInt32 intValue) {
            this.expectedIntValue = intValue;
            this.inputSimType = simType;

            whenCalculatedUInt32();
            thenCalculatedUInt32IsCorrect();
        }

        private void whenCalculatedUInt32() {
            this.calculatedUInt32 = this.inputSimType.toUInt32();
        }

        private void thenCalculatedUInt32IsCorrect() {
            assertEquals(this.expectedIntValue, this.calculatedUInt32);
        }
    }
}
