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
package org.freedesktop.modemmanager1.modem;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class ListTuple<A, B> extends Tuple {
    @Position(0)
    private A selected;
    @Position(1)
    private B installed;

    public ListTuple(A selected, B installed) {
        this.selected = selected;
        this.installed = installed;
    }

    public void setSelected(A arg) {
        selected = arg;
    }

    public A getSelected() {
        return selected;
    }

    public void setInstalled(B arg) {
        installed = arg;
    }

    public B getInstalled() {
        return installed;
    }

}
