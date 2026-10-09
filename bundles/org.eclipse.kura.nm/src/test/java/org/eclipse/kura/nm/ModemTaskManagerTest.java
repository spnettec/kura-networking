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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.networkmanager.Device;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@Timeout(10)
public class ModemTaskManagerTest {
    private ScheduledTasks scheduledTasks;
    private MockedStatic<NMDbusConnector> connectorFactory;

    @BeforeEach
    void schedulerSetup() {
        this.scheduledTasks = new ScheduledTasks();
        this.connectorFactory = Mockito.mockStatic(NMDbusConnector.class);
        this.connectorFactory.when(NMDbusConnector::getInstance).thenReturn(mock(NMDbusConnector.class));
    }

    @AfterEach
    void schedulerCleanup() {
        try {
            if (this.modemTaskManager != null) {
                this.modemTaskManager.modemTaskHandlerDisable();
            }
        } finally {
            this.connectorFactory.close();
            this.scheduledTasks.close();
        }
    }

    private DBusConnection dbusConnection;
    private NetworkProperties networkProperties;
    private ModemTaskManager modemTaskManager;

    @Test
    public void shouldEnableModemTaskHandler() throws DBusException {
        givenNMDbusConnectorMock();
        givenNetworkProperties("1-4", true, 1);
        givenModemTaskManager();

        whenEnableModemTaskHandler("1-4");

        thenModemTaskHandlerPresent("1-4");
    }

    @Test
    public void shouldNotAddSignalHandlerIfNotAutoconnectOrResetTimeout() throws DBusException {
        givenNMDbusConnectorMock();
        givenNetworkProperties("1-4", false, 0);
        givenModemTaskManager();

        whenEnableModemTaskHandler("1-4");

        thenSignalHandlerIsNotAdded();
    }

    @Test
    public void shouldAddSignalHandlerIfAutoconnect() throws DBusException {
        givenNMDbusConnectorMock();
        givenNetworkProperties("1-4", true, 0);
        givenModemTaskManager();

        whenEnableModemTaskHandler("1-4");

        thenSignalHandlerIsAdded(1);
    }

    @Test
    public void shouldAddSignalHandlerIfResetTimeout() throws DBusException {
        givenNMDbusConnectorMock();
        givenNetworkProperties("1-4", false, 10);
        givenModemTaskManager();

        whenEnableModemTaskHandler("1-4");

        thenSignalHandlerIsAdded(1);
    }

    @Test
    public void shouldNotAddModemTaskHandlerIfAlreadyActivated() throws DBusException {
        givenNMDbusConnectorMock();
        givenNetworkProperties("1-4", true, 10);
        givenModemTaskManager();

        whenEnableModemTaskHandler("1-4");
        whenEnableModemTaskHandler("1-4");

        thenSignalHandlerIsAdded(1);
    }

    @Test
    public void shouldDisableModemTaskHandler() throws DBusException {
        givenNMDbusConnectorMock();
        givenNetworkProperties("1-4", true, 1);
        givenModemTaskManager();

        whenEnableModemTaskHandler("1-4");
        whenDisableModemTaskHandler("1-4");

        thenSignalHandlerIsRemoved(1);
        thenModemTaskHandlerNotPresent("1-4");
    }

    /**
     * Given
     *
     */
    private void givenNMDbusConnectorMock() {
        this.dbusConnection = mock(DBusConnection.class);
    }

    private void givenNetworkProperties(String deviceId, boolean autoconnect, int resetTimeout) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(String.format("net.interface.%s.config.holdoff", deviceId), 10);
        properties.put(String.format("net.interface.%s.config.maxFail", deviceId), 3);
        properties.put(String.format("net.interface.%s.config.persist", deviceId), autoconnect);
        properties.put(String.format("net.interface.%s.config.resetTimeout", deviceId), resetTimeout);
        this.networkProperties = new NetworkProperties(properties);
    }

    private void givenModemTaskManager() {
        this.modemTaskManager = new ModemTaskManager(this.dbusConnection);
    }

    /**
     * When
     *
     */
    private void whenEnableModemTaskHandler(String deviceId) throws DBusException {
        this.scheduledTasks.capture(() ->
                this.modemTaskManager.modemTaskHandlerEnable(deviceId, mock(Device.class), this.networkProperties));
    }

    private void whenDisableModemTaskHandler(String deviceId) {
        this.modemTaskManager.modemTaskHandlerDisable(deviceId);
    }

    /*
     * Then
     *
     */

    private void thenModemTaskHandlerPresent(String deviceId) {
        assertTrue(this.modemTaskManager.isModemTaskHandlerPresent(deviceId));
    }

    private void thenModemTaskHandlerNotPresent(String deviceId) {
        assertTrue(!this.modemTaskManager.isModemTaskHandlerPresent(deviceId));
    }

    private void thenSignalHandlerIsNotAdded() throws DBusException {
        verify(this.dbusConnection, never())
                .addSigHandler(eq(org.freedesktop.networkmanager.Device.StateChanged.class), any());
    }

    private void thenSignalHandlerIsAdded(int times) throws DBusException {
        verify(this.dbusConnection, times(times))
                .addSigHandler(eq(org.freedesktop.networkmanager.Device.StateChanged.class), any());
    }

    private void thenSignalHandlerIsRemoved(int times) throws DBusException {
        verify(this.dbusConnection, times(times))
                .removeSigHandler(eq(org.freedesktop.networkmanager.Device.StateChanged.class), any());
    }
}
