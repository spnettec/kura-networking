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
package org.freedesktop.networkmanager.vpn;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.VPN.Connection")
@DBusProperty(name = "VpnState", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Banner", type = String.class, access = Access.READ)
public interface Connection extends DBusInterface {

    public static class VpnStateChanged extends DBusSignal {

        private final UInt32 state;
        private final UInt32 reason;

        public VpnStateChanged(String path, UInt32 state, UInt32 reason) throws DBusException {
                super(path, state, reason);        this.state = state;
                this.reason = reason;
        }

        public UInt32 getState() {
            return state;
        }

        public UInt32 getReason() {
            return reason;
        }

    }

}
