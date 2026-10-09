package org.eclipse.kura.nm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

public class KuraWifiSecurityTypeTest {

    @org.junit.jupiter.api.Nested
    public class KuraWifiSecurityTypeFromStringTestErrors {

        private Exception occurredException;
        KuraWifiSecurityType securityType;
        private String inputSecurityType;

        public static Collection<Object[]> SimTypeParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {"UNKNOWN"});
            params.add(new Object[] {null});
            params.add(new Object[] {""});
            return params;
        }



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("SimTypeParams")
        public void shouldThrowException(String securityType) {
            this.inputSecurityType = securityType;

            whenFromStringIsCalledWith(this.inputSecurityType);
            thenExceptionOccurred(IllegalArgumentException.class);
        }

        public void whenFromStringIsCalledWith(String securityType) {
            try {
                this.securityType = KuraWifiSecurityType.fromString(securityType);
            } catch (Exception e) {
                this.occurredException = e;
            }
        }

        private <E extends Exception> void thenExceptionOccurred(Class<E> expectedExceptionClass) {
            assertNotNull(this.occurredException);
            assertEquals(expectedExceptionClass, this.occurredException.getClass());
        }
    }

    @org.junit.jupiter.api.Nested
    public class KuraWifiSecurityTypeFromStringTest {

        private String inputSecurityType;
        private KuraWifiSecurityType expectedSecurityType;
        private KuraWifiSecurityType outputSecurityType;

        public static Collection<Object[]> SimTypeParams() {
            List<Object[]> params = new ArrayList<>();
            params.add(new Object[] {"NONE", KuraWifiSecurityType.SECURITY_NONE});
            params.add(new Object[] {"SECURITY_WEP", KuraWifiSecurityType.SECURITY_WEP});
            params.add(new Object[] {"SECURITY_WPA", KuraWifiSecurityType.SECURITY_WPA});
            params.add(new Object[] {"SECURITY_WPA2", KuraWifiSecurityType.SECURITY_WPA2});
            params.add(new Object[] {"SECURITY_WPA_WPA2", KuraWifiSecurityType.SECURITY_WPA_WPA2});
            return params;
        }



        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.MethodSource("SimTypeParams")
        public void shouldWork(String inputSecurityType, KuraWifiSecurityType expectedSecurityType) {
            this.inputSecurityType = inputSecurityType;
            this.expectedSecurityType = expectedSecurityType;

            whenFromStringIsCalledWith(this.inputSecurityType);
            thenSecurityTypeIs(this.expectedSecurityType);
        }

        public void whenFromStringIsCalledWith(String securityType) {
            this.outputSecurityType = KuraWifiSecurityType.fromString(securityType);
        }

        private void thenSecurityTypeIs(KuraWifiSecurityType securityType) {
            assertEquals(this.expectedSecurityType, this.outputSecurityType);
        }
    }
}
