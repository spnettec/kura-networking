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
package org.freedesktop.networkmanager.settings;

import java.util.Map;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

/**
 * Auto-generated class.
 */
@DBusInterfaceName("org.freedesktop.NetworkManager.Settings.Connection")
@DBusProperty(name = "Unsaved", type = Boolean.class, access = Access.READ)
@DBusProperty(name = "Flags", type = UInt32.class, access = Access.READ)
@DBusProperty(name = "Filename", type = String.class, access = Access.READ)
public interface Connection extends DBusInterface {

    void Update(Map<String, Map<String, Variant<?>>> properties);

    void UpdateUnsaved(Map<String, Map<String, Variant<?>>> properties);

    void Delete();

    Map<String, Map<String, Variant<?>>> GetSettings();

    Map<String, Map<String, Variant<?>>> GetSecrets(String settingName);

    void ClearSecrets();

    void Save();

    Map<String, Variant<?>> Update2(Map<String, Map<String, Variant<?>>> settings, UInt32 flags, Map<String, Variant<?>> args);

    public static class Updated extends DBusSignal {

        public Updated(String path) throws DBusException {
                super(path);
        }

    }

    public static class Removed extends DBusSignal {

        public Removed(String path) throws DBusException {
                super(path);
        }

    }

}
