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
public class LoadConnectionsTuple<A, B> extends Tuple {
    @Position(0)
    private A status;
    @Position(1)
    private B failures;

    public LoadConnectionsTuple(A status, B failures) {
        this.status = status;
        this.failures = failures;
    }

    public void setStatus(A arg) {
        status = arg;
    }

    public A getStatus() {
        return status;
    }

    public void setFailures(B arg) {
        failures = arg;
    }

    public B getFailures() {
        return failures;
    }

}
