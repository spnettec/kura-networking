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
package org.freedesktop;

import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.Position;

/**
 * Auto-generated class.
 */
public class GetLoggingTuple<A, B> extends Tuple {
    @Position(0)
    private A level;
    @Position(1)
    private B domains;

    public GetLoggingTuple(A level, B domains) {
        this.level = level;
        this.domains = domains;
    }

    public void setLevel(A arg) {
        level = arg;
    }

    public A getLevel() {
        return level;
    }

    public void setDomains(B arg) {
        domains = arg;
    }

    public B getDomains() {
        return domains;
    }

}
