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
package org.freedesktop.networkmanager.device;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Device.Tun")
@DBusProperty(name = "Owner", type = Long.class, access = Access.READ)
@DBusProperty(name = "Group", type = Long.class, access = Access.READ)
@DBusProperty(name = "Mode", type = String.class, access = Access.READ)
@DBusProperty(name = "NoPi", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "VnetHdr", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "MultiQueue", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "HwAddress", type = String.class, access = Access.READ)
public interface Tun extends DBusInterface {

}
