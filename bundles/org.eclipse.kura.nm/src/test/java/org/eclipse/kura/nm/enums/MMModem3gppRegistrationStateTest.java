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
import org.eclipse.kura.net.status.modem.RegistrationStatus;
import org.freedesktop.dbus.types.UInt32;
import org.junit.jupiter.api.Test;

public class MMModem3gppRegistrationStateTest {

    @org.junit.jupiter.api.Nested
    public class MMModem3gppRegistrationStatetoMMModem3gppRegistrationStateTest {

        public static Collection<Object[]> MMModem3gppRegistrationStateParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(
                    new Object[] {new UInt32(0), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_IDLE});
            params.add(
                    new Object[] {new UInt32(1), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_HOME});
            params.add(
                    new Object[] {new UInt32(2), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_SEARCHING
                    });
            params.add(
                    new Object[] {new UInt32(3), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_DENIED});
            params.add(
                    new Object[] {new UInt32(4), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_UNKNOWN
                    });
            params.add(
                    new Object[] {new UInt32(5), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ROAMING
                    });
            params.add(new Object[] {
                new UInt32(6), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_HOME_SMS_ONLY
            });
            params.add(new Object[] {
                new UInt32(7), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ROAMING_SMS_ONLY
            });
            params.add(new Object[] {
                new UInt32(8), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_EMERGENCY_ONLY
            });
            params.add(new Object[] {
                new UInt32(9), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_HOME_CSFB_NOT_PREFERRED
            });
            params.add(new Object[] {
                new UInt32(10), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ROAMING_CSFB_NOT_PREFERRED
            });
            params.add(new Object[] {
                new UInt32(11), MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ATTACHED_RLOS
            });
            return params;
        }

        private UInt32 inputIntValue;
        private MMModem3gppRegistrationState expectedRegistrationState;
        private MMModem3gppRegistrationState calculatedRegistrationState;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("MMModem3gppRegistrationStateParams")
        public void shouldReturnCorrectRegistrationState(UInt32 intValue, MMModem3gppRegistrationState registrationState) {
            this.inputIntValue = intValue;
            this.expectedRegistrationState = registrationState;

            whenCalculateMMModem3gppRegistrationState();
            thenCalculatedMMModem3gppRegistrationStateIsCorrect();
        }

        private void whenCalculateMMModem3gppRegistrationState() {
            this.calculatedRegistrationState =
                    MMModem3gppRegistrationState.toMMModem3gppRegistrationState(this.inputIntValue);
        }

        private void thenCalculatedMMModem3gppRegistrationStateIsCorrect() {
            assertEquals(this.expectedRegistrationState, this.calculatedRegistrationState);
        }
    }

    @org.junit.jupiter.api.Nested
    public class MMModem3gppRegistrationStatetoRegistrationStatusTest {

        public static Collection<Object[]> MMModem3gppRegistrationStateParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {new UInt32(0), RegistrationStatus.IDLE});
            params.add(new Object[] {new UInt32(1), RegistrationStatus.HOME});
            params.add(new Object[] {new UInt32(2), RegistrationStatus.SEARCHING});
            params.add(new Object[] {new UInt32(3), RegistrationStatus.DENIED});
            params.add(new Object[] {new UInt32(4), RegistrationStatus.UNKNOWN});
            params.add(new Object[] {new UInt32(5), RegistrationStatus.ROAMING});
            params.add(new Object[] {new UInt32(6), RegistrationStatus.HOME_SMS_ONLY});
            params.add(new Object[] {new UInt32(7), RegistrationStatus.ROAMING_SMS_ONLY});
            params.add(new Object[] {new UInt32(8), RegistrationStatus.EMERGENCY_ONLY});
            params.add(new Object[] {new UInt32(9), RegistrationStatus.HOME_CSFB_NOT_PREFERRED});
            params.add(new Object[] {new UInt32(10), RegistrationStatus.ROAMING_CSFB_NOT_PREFERRED});
            params.add(new Object[] {new UInt32(11), RegistrationStatus.ATTACHED_RLOS});
            return params;
        }

        private UInt32 inputIntValue;
        private RegistrationStatus expectedRegistrationStatus;
        private RegistrationStatus calculatedRegistrationStatus;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("MMModem3gppRegistrationStateParams")
        public void shouldReturnCorrectRegistrationStatus(UInt32 intValue, RegistrationStatus registrationStatus) {
            this.inputIntValue = intValue;
            this.expectedRegistrationStatus = registrationStatus;

            whenCalculatedRegistrationStatus();
            thenCalculatedRegistrationStatusTypeIsCorrect();
        }

        private void whenCalculatedRegistrationStatus() {
            this.calculatedRegistrationStatus = MMModem3gppRegistrationState.toRegistrationStatus(this.inputIntValue);
        }

        private void thenCalculatedRegistrationStatusTypeIsCorrect() {
            assertEquals(this.expectedRegistrationStatus, this.calculatedRegistrationStatus);
        }
    }

    @org.junit.jupiter.api.Nested
    public class MMModem3gppRegistrationStateToUInt32Test {

        public static Collection<Object[]> MMModem3gppRegistrationStateParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(
                    new Object[] {MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_IDLE, new UInt32(0)});
            params.add(
                    new Object[] {MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_HOME, new UInt32(1)});
            params.add(
                    new Object[] {MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_SEARCHING, new UInt32(2)
                    });
            params.add(
                    new Object[] {MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_DENIED, new UInt32(3)});
            params.add(
                    new Object[] {MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_UNKNOWN, new UInt32(4)
                    });
            params.add(
                    new Object[] {MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ROAMING, new UInt32(5)
                    });
            params.add(new Object[] {
                MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_HOME_SMS_ONLY, new UInt32(6)
            });
            params.add(new Object[] {
                MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ROAMING_SMS_ONLY, new UInt32(7)
            });
            params.add(new Object[] {
                MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_EMERGENCY_ONLY, new UInt32(8)
            });
            params.add(new Object[] {
                MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_HOME_CSFB_NOT_PREFERRED, new UInt32(9)
            });
            params.add(new Object[] {
                MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ROAMING_CSFB_NOT_PREFERRED, new UInt32(10)
            });
            params.add(new Object[] {
                MMModem3gppRegistrationState.MM_MODEM_3GPP_REGISTRATION_STATE_ATTACHED_RLOS, new UInt32(11)
            });
            return params;
        }

        private MMModem3gppRegistrationState inputRegistrationState;
        private UInt32 expectedIntValue;
        private UInt32 calculatedUInt32;



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("MMModem3gppRegistrationStateParams")
        public void shouldReturnCorrectUInt32(MMModem3gppRegistrationState registrattionState, UInt32 intValue) {
            this.expectedIntValue = intValue;
            this.inputRegistrationState = registrattionState;

            whenCalculatedUInt32();
            thenCalculatedUInt32IsCorrect();
        }

        private void whenCalculatedUInt32() {
            this.calculatedUInt32 = this.inputRegistrationState.toUInt32();
        }

        private void thenCalculatedUInt32IsCorrect() {
            assertEquals(this.expectedIntValue, this.calculatedUInt32);
        }
    }
}
