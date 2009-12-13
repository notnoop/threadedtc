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
