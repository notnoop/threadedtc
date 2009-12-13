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
