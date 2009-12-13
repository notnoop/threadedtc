/*
 * Copyright 2009 Mahmood Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.notnoop.threadedtc.internal;

import java.util.Arrays;
import java.util.List;

public class ThreadGroupUtils {

    public static boolean areAnyThreadsAlive(ThreadGroup tg) {
        for (Thread t : getThreads(tg)) {
            switch (t.getState()) {
            case NEW: case TERMINATED:
                break;
            default:
                return true;
            }
        }
        return false;
    }

    public static boolean areAnyThreadsRunning(ThreadGroup tg) {
        for (Thread t : getThreads(tg)) {
            switch (t.getState()) {
            case RUNNABLE:
                return true;
            }
        }
        return false;
    }

    public static boolean areAnyThreadsInTimedWaiting(ThreadGroup tg) {
        for (Thread t : getThreads(tg)) {
            switch (t.getState()) {
            case TIMED_WAITING:
                return true;
            }
        }
        return false;
    }

    public static List<Thread> getThreads(ThreadGroup tg) {
        return getThreads(tg, true);
    }

    public static List<Thread> getThreads(ThreadGroup tg, boolean recursive) {
        return getThreads(tg, recursive, tg.activeCount() + 10);
    }

    public static List<Thread> getThreads(ThreadGroup tg, boolean recursive, int estimate) {
        Thread[] threads = new Thread[estimate];
        int count = tg.enumerate(threads, recursive);
        if (count == estimate)
            return getThreads(tg, recursive, estimate + 10);
        else
            return Arrays.asList(threads).subList(0, count);
    }
}
