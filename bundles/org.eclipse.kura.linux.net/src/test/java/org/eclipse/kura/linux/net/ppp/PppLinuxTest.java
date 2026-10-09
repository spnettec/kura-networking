/*******************************************************************************
 * Copyright (c) 2021, 2026 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 ******************************************************************************/
package org.eclipse.kura.linux.net.ppp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.linux.executor.LinuxSignal;
import org.eclipse.kura.executor.CommandExecutorService;
import org.eclipse.kura.executor.Pid;
import org.eclipse.kura.executor.Signal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@org.junit.jupiter.api.Timeout(15)
public class PppLinuxTest {

    @Test
    public void shouldTerminatePppdWithSigterm() {
        givenProcess("/usr/sbin/pppd /dev/ttyACM0 call ppp1", 10, LinuxSignal.SIGTERM);

        whenPppdIsStoppedFor("ppp1", "/dev/ttyACM0");

        thenProcessReceivedSignals(LinuxSignal.SIGTERM);
        thenProcessIsStopped();
    }

    @Test
    public void shouldTerminatePppdWithSigkill() {
        givenProcess("/usr/sbin/pppd /dev/ttyACM0 call ppp1", 10, LinuxSignal.SIGKILL);

        whenPppdIsStoppedFor("ppp1", "/dev/ttyACM0");

        thenProcessReceivedSignals(LinuxSignal.SIGTERM, LinuxSignal.SIGKILL);
        thenProcessIsStopped();
    }

    private final CommandExecutorService commandExecutorService = Mockito.mock(CommandExecutorService.class);
    private final PppLinux pppLinux = new PppLinux(commandExecutorService);
    private Optional<Process> process = Optional.empty();

    public PppLinuxTest() {
        Mockito.when(commandExecutorService.getPids(Mockito.any())).thenAnswer(i -> {
            final String[] requestedCommand = i.getArgument(0, String[].class);

            final String requestedCommandConcat = concat(requestedCommand, " ");

            return process
                    .map(Collections::singletonList)
                    .orElseThrow(() -> new IllegalStateException("process not configured"))
                    .stream()
                    .filter(p -> p.commandLine.contains(requestedCommandConcat) && p.stopped == false)
                    .collect(Collectors.toMap(p -> p.commandLine, p -> p.pid));
        });

        Mockito.when(commandExecutorService.stop(Mockito.any(), Mockito.any())).then(i -> {
            final Pid pid = i.getArgument(0, Pid.class);
            final Signal signal = i.getArgument(1, Signal.class);

            final Optional<Process> targetProcess = process.filter(p -> p.pid.getPid() == pid.getPid());

            if (targetProcess.isPresent()) {
                targetProcess.get().sendSignal(signal);
                return true;
            } else {
                return false;
            }
        });

        Mockito.when(commandExecutorService.isRunning(Mockito.any(Pid.class))).thenAnswer(i -> {
            final Pid pid = i.getArgument(0, Pid.class);

            return process.filter(p -> p.pid.getPid() == pid.getPid() && !p.stopped)
                    .isPresent();
        });
    }

    private void thenProcessReceivedSignals(final Signal... signals) {
        final Process currentProcess = process.orElseThrow(() -> new IllegalStateException("process not configured"));

        assertEquals(Arrays.asList(signals), currentProcess.receivedSignals);
    }

    private void thenProcessIsStopped() {
        final Process currentProcess = process.orElseThrow(() -> new IllegalStateException("process not configured"));

        assertTrue(currentProcess.stopped, "process is not stopped");
    }

    private void givenProcess(final String command, final int pid, final Signal requiredStopSignal) {
        process = Optional.of(new Process(command, () -> pid, requiredStopSignal));
    }

    private void whenPppdIsStoppedFor(final String iface, final String port) {
        // Resolve classes before intercepting File construction, which also affects class loading.
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                Class.forName("org.eclipse.kura.linux.net.util.ProcessStopUtil"));
        // Disconnect also probes a /var/lock file; never touch the host lock directory.
        try (org.mockito.MockedConstruction<java.io.File> locks = Mockito.mockConstruction(java.io.File.class,
                (file, context) -> assertEquals("/var/lock/LCK..ttyACM0", context.arguments().get(0)))) {
            pppLinux.disconnect(iface, port);
            assertEquals(1, locks.constructed().size());
        } catch (final KuraException e) {
            fail("failed to disconnect " + e);
        }
    }

    private String concat(final String[] value, final String delim) {
        return String.join(delim, value);
    }

    private static class Process {

        private final String commandLine;
        private final Pid pid;
        private final Signal stopSignal;
        private List<Signal> receivedSignals = new ArrayList<>();
        private boolean stopped = false;

        public Process(final String commandLine, final Pid pid, final Signal stopSignal) {
            this.commandLine = commandLine;
            this.pid = pid;
            this.stopSignal = stopSignal;
        }

        private void sendSignal(final Signal signal) {
            receivedSignals.add(signal);
            if (signal == stopSignal) {
                stopped = true;
            }
        }
    }
}
