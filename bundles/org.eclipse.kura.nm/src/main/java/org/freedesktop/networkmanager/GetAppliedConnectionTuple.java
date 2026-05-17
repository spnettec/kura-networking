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
public class GetAppliedConnectionTuple<A, B> extends Tuple {
    @Position(0)
    private A connection;
    @Position(1)
    private B versionId;

    public GetAppliedConnectionTuple(A connection, B versionId) {
        this.connection = connection;
        this.versionId = versionId;
    }

    public void setConnection(A arg) {
        connection = arg;
    }

    public A getConnection() {
        return connection;
    }

    public void setVersionId(B arg) {
        versionId = arg;
    }

    public B getVersionId() {
        return versionId;
    }

}
