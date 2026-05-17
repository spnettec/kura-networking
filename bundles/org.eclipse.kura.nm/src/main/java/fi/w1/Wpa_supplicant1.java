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
package fi.w1;

import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.DBusProperty.Access;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.Variant;

/**
 * Auto-generated class.
 */
@DBusProperty(name = "DebugLevel", type = String.class, access = Access.READ_WRITE)
@DBusProperty(name = "DebugTimestamp", type = Boolean.class, access = Access.READ_WRITE)
@DBusProperty(name = "DebugShowKeys", type = Boolean.class, access = Access.READ_WRITE)
@DBusProperty(name = "Interfaces", type = Wpa_supplicant1.PropertyInterfacesType.class, access = Access.READ)
@DBusProperty(name = "EapMethods", type = Wpa_supplicant1.PropertyEapMethodsType.class, access = Access.READ)
@DBusProperty(name = "Capabilities", type = Wpa_supplicant1.PropertyCapabilitiesType.class, access = Access.READ)
@DBusProperty(name = "WFDIEs", type = Wpa_supplicant1.PropertyWFDIEsType.class, access = Access.READ_WRITE)
public interface Wpa_supplicant1 extends DBusInterface {

    DBusPath CreateInterface(Map<String, Variant<?>> args);

    void RemoveInterface(DBusPath path);

    DBusPath GetInterface(String ifname);

    void ExpectDisconnect();

    public static class InterfaceAdded extends DBusSignal {

        private final DBusPath dbusPath;
        private final Map<String, Variant<?>> properties;

        public InterfaceAdded(String path, DBusPath dbusPath, Map<String, Variant<?>> properties) throws DBusException {
                super(path, dbusPath, properties);        this.dbusPath = dbusPath;
                this.properties = properties;
        }

        public DBusPath getDbusPath() {
            return dbusPath;
        }

        public Map<String, Variant<?>> getProperties() {
            return properties;
        }

    }

    public static class InterfaceRemoved extends DBusSignal {

        private final DBusPath dbusPath;

        public InterfaceRemoved(String path, DBusPath dbusPath) throws DBusException {
                super(path, dbusPath);        this.dbusPath = dbusPath;
        }

        public DBusPath getDbusPath() {
            return dbusPath;
        }

    }

    public static class PropertiesChanged extends DBusSignal {

        private final Map<String, Variant<?>> properties;

        public PropertiesChanged(String path, Map<String, Variant<?>> properties) throws DBusException {
                super(path, properties);        this.properties = properties;
        }

        public Map<String, Variant<?>> getProperties() {
            return properties;
        }

    }

    public static interface PropertyInterfacesType extends TypeRef<List<DBusPath>> {

    }

    public static interface PropertyEapMethodsType extends TypeRef<List<String>> {

    }

    public static interface PropertyCapabilitiesType extends TypeRef<List<String>> {

    }

    public static interface PropertyWFDIEsType extends TypeRef<List<Byte>> {

    }

}
