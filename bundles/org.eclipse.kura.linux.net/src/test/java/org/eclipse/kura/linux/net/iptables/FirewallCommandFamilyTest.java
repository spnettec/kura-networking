/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.kura.linux.net.iptables;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.kura.core.linux.executor.LinuxExitStatus;
import org.eclipse.kura.executor.Command;
import org.eclipse.kura.executor.CommandExecutorService;
import org.eclipse.kura.executor.CommandStatus;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class FirewallCommandFamilyTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void saveUsesTheConfiguredAddressFamily(boolean ipv6) {
        CommandExecutorService executor = executor();
        IptablesConfig config = ipv6 ? new IptablesConfigIPv6(executor) : new IptablesConfig(executor);
        String file = directory.resolve("rules").toString();

        config.save(file);

        ArgumentCaptor<Command> command = ArgumentCaptor.forClass(Command.class);
        verify(executor).execute(command.capture());
        assertArrayEquals(new String[] {ipv6 ? "ip6tables-save" : "iptables-save", ">", file},
                command.getValue().getCommandLine());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void restoreUsesTheConfiguredAddressFamilyAndRemovesItsInput(boolean ipv6) throws Exception {
        CommandExecutorService executor = executor();
        IptablesConfig config = ipv6 ? new IptablesConfigIPv6(executor) : new IptablesConfig(executor);
        Path file = Files.writeString(directory.resolve("rules"), "*filter\nCOMMIT\n");

        config.restore(file.toString());

        ArgumentCaptor<Command> command = ArgumentCaptor.forClass(Command.class);
        verify(executor).execute(command.capture());
        assertArrayEquals(new String[] {ipv6 ? "ip6tables-restore" : "iptables-restore", file.toString()},
                command.getValue().getCommandLine());
        assertFalse(Files.exists(file));
    }

    private CommandExecutorService executor() {
        CommandExecutorService executor = mock(CommandExecutorService.class);
        when(executor.execute(any(Command.class))).thenReturn(new CommandStatus(null, new LinuxExitStatus(0)));
        return executor;
    }
}
