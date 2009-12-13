package com.notnoop.threadedtc;

public class RunnerConductor {
    static Conductor conductor;

    public static Thread thread(Runnable fun) { 
        return conductor.thread(fun);
    }

    public static Thread thread(TCRunnable fun) { 
        return conductor.thread(fun);
    }

    public static Thread thread(String name, Runnable fun) {
        return conductor.thread(name, fun);
    }

    public static Thread thread(String name, TCRunnable fun) {
        return conductor.thread(name, fun);
    }

    public static void whenFinished(Runnable fun) {
        conductor.whenFinished(fun);
    }

    public static void whenFinished(TCRunnable fun) {
        conductor.whenFinished(fun);
    }

    public static void waitForBeat(int beat) {
        conductor.waitForBeat(beat);
    }

    public static int beat() {
        return conductor.beat();
    }

    public static void withConductorFrozen(Runnable fun) {
        conductor.withConductorFrozen(fun);
    }

    public static void withConductorFrozen(TCRunnable fun) {
        conductor.withConductorFrozen(fun);
    }

    public static boolean isConductorFrozen() {
        return conductor.isConductorFrozen();
    }

    public static void conduct() {
        conductor.conduct();
    }

    public static boolean conductingHasBegun() {
        return conductor.conductingHasBegun();
    }

    public static void conduct(int clockPeriod, int timeout) {
        conductor.conduct(clockPeriod, timeout);
    }

    public static Thread getThread(String name) {
        return conductor.getThread(name);
    }
}
