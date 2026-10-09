/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.kura.nm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;

/** Drives production callbacks at their requested deadlines without creating threads. */
public final class ScheduledTasks implements AutoCloseable {
    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final PriorityQueue<Task> pending = new PriorityQueue<>(
            Comparator.comparingLong((Task task) -> task.due).thenComparingLong(task -> task.sequence));
    private long now;
    private long sequence;
    private boolean shutdown;

    public ScheduledTasks() {
        doAnswer(call -> enqueue(call.getArgument(0), call.getArgument(1), 0, call.getArgument(2)))
                .when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        doAnswer(call -> enqueue(call.getArgument(0), call.getArgument(1), call.getArgument(2), call.getArgument(3)))
                .when(executor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        doAnswer(call -> enqueue(call.getArgument(0), call.getArgument(1), call.getArgument(2), call.getArgument(3)))
                .when(executor).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        doAnswer(call -> { shutdown = true; pending.clear(); return null; }).when(executor).shutdown();
        when(executor.shutdownNow()).thenAnswer(call -> { shutdown = true; pending.clear(); return List.of(); });
        when(executor.isShutdown()).thenAnswer(call -> shutdown);
        when(executor.isTerminated()).thenAnswer(call -> shutdown);
        try {
            when(executor.awaitTermination(anyLong(), any(TimeUnit.class))).thenAnswer(call -> shutdown);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /** Intercepts only component construction/startup, never the JUnit timeout executor. */
    public void capture(Executable action) {
        try (MockedStatic<Executors> factories = mockStatic(Executors.class)) {
            factories.when(Executors::newSingleThreadScheduledExecutor).thenReturn(executor);
            factories.when(() -> Executors.newSingleThreadScheduledExecutor(any(ThreadFactory.class))).thenReturn(executor);
            factories.when(() -> Executors.newScheduledThreadPool(anyInt())).thenReturn(executor);
            action.execute();
        } catch (Throwable e) {
            throw new AssertionError("Component startup failed", e);
        }
    }

    private ScheduledFuture<?> enqueue(Runnable callback, long delay, long period, TimeUnit unit) {
        if (shutdown) {
            throw new RejectedExecutionException("Test scheduler is shut down");
        }
        Task task = new Task(callback, now + unit.toMillis(delay), unit.toMillis(period), sequence++);
        pending.add(task);
        return task.future;
    }

    public void advance(Duration duration) {
        long target = now + duration.toMillis();
        int ticks = 0;
        while (!pending.isEmpty() && pending.peek().due <= target) {
            if (++ticks > 1000) {
                throw new AssertionError("Unbounded callback rescheduling");
            }
            Task task = pending.remove();
            now = task.due;
            if (!task.cancelled) {
                task.callback.run();
                if (task.period > 0 && !task.cancelled && !shutdown) {
                    task.due += task.period;
                    pending.add(task);
                } else {
                    task.done = true;
                }
            }
        }
        now = target;
    }

    @Override
    public void close() {
        pending.clear();
    }

    private static final class Task {
        private final Runnable callback;
        private final long period;
        private final long sequence;
        private final ScheduledFuture<?> future = mock(ScheduledFuture.class);
        private long due;
        private boolean cancelled;
        private boolean done;

        private Task(Runnable callback, long due, long period, long sequence) {
            this.callback = callback;
            this.due = due;
            this.period = period;
            this.sequence = sequence;
            when(future.cancel(anyBoolean())).thenAnswer(call -> { cancelled = true; return true; });
            when(future.isCancelled()).thenAnswer(call -> cancelled);
            when(future.isDone()).thenAnswer(call -> done || cancelled);
        }
    }
}
