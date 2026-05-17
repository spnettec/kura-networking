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
package org.freedesktop.networkmanager;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class AddConnection2Tuple<A, B> extends Tuple {
    @Position(0)
    private A path;
    @Position(1)
    private B result;

    public AddConnection2Tuple(A path, B result) {
        this.path = path;
        this.result = result;
    }

    public void setPath(A arg) {
        path = arg;
    }

    public A getPath() {
        return path;
    }

    public void setResult(B arg) {
        result = arg;
    }

    public B getResult() {
        return result;
    }

}
