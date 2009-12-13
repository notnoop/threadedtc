/*
 * Copyright 2001-2009 Artima, Inc.
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

/*
 * Keeps the main thread from allowing the test threads to execute their bodies
 * until all of them are started, and ready to go. When a test thread is started,
 * it will call increment from its constructor. It then calls decrement from its
 * run method. Test threads are started immediately by the thread() methods, and
 * so this allows the main thread to block until all test threads have started.
 * It does this by calling the waitUntilAllTestThreadsHaveStarted method, which
 * blocks in the wait set if the count is not 0. (The count is only non-zero when
 * one or more test threads have been created but not yet gotten their run methods
 * going.) This is only used for threads started by the main thread. By the time
 * conduct is invoked, all threads started by the main thread will have called
 * increment. (Increment in this case will be called by the main thread.) After
 * those threads go, they may actually call thread method again, but the main thread
 * will only call waitUntilAllTestThreadsHaveStarted once, so it won't matter. - bv
 */
public class TestThreadsStartingCounter {
    private int count = 0;

    public synchronized void increment() {
        count += 1;
    }

    public synchronized void decrement() {
        count -= 1;
        notifyAll();
    }

    public synchronized void waitUntilAllTestThreadsHaveStarted() throws InterruptedException {
        while (count != 0) {
            wait();
        }
    }
}