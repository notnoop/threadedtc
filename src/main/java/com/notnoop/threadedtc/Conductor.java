package com.notnoop.threadedtc;

/*
 * Copyright 2001-2008 Artima, Inc.
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

import static com.notnoop.threadedtc.internal.ThreadGroupUtils.*;
import static com.notnoop.threadedtc.internal.RunnableUtils.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.notnoop.threadedtc.exceptions.DeadlockSuspectedError;
import com.notnoop.threadedtc.exceptions.NotAllowedException;
import com.notnoop.threadedtc.exceptions.TimeoutError;
import com.notnoop.threadedtc.internal.TestThreadsStartingCounter;

/**
 * Class that facilitates the testing of classes, traits, and libraries designed
 * to be used by multiple threads concurrently.
 *
 * <p>
 * A <code>Conductor</code> conducts a multi-threaded scenario by maintaining
 * a clock of "beats." Beats are numbered starting with 0. You can ask a
 * <code>Conductor</code> to run threads that interact with the class, trait,
 * or library (the <em>subject</em>)
 * you want to test. A thread can call the <code>Conductor</code>'s
 * <code>waitForBeat</code> method, which will cause the thread to block
 * until that beat has been reached. The <code>Conductor</code> will advance
 * the beat only when all threads participating in the test are blocked. By
 * tying the timing of thread activities to specific beats, you can write
 * tests for concurrent systems that have deterministic interleavings of
 * threads.
 * </p>
 *
 * <p>
 * A <code>Conductor</code> object has a three-phase lifecycle. It begins its life
 * in the <em>setup</em> phase. During this phase, you can start threads by
 * invoking the <code>thread</code> method on the <code>Conductor</code>.
 * When <code>conduct</code> is invoked on a <code>Conductor</code>, it enters
 * the <em>conducting</em> phase. During this phase it conducts the one multi-threaded
 * scenario it was designed to conduct. After all participating threads have exited, either by
 * returning normally or throwing an exception, the <code>conduct</code> method
 * will complete, either by returning normally or throwing an exception. As soon as
 * the <code>conduct</code> method completes, the <code>Conductor</code>
 * enters its <em>defunct</em> phase. Once the <code>Conductor</code> has conducted
 * a multi-threaded scenario, it is defunct and can't be reused. To run the same test again,
 * you'll need to create a new instance of <code>Conductor</code>.
 * </p>
 *
 * <p>
 * Here's an example of the use of <code>Conductor</code> to test the <code>ArrayBlockingQueue</code>
 * class from <code>java.util.concurrent</code>:
 * </p>
 *
 * <pre>
 * import java.util.concurrent.ArrayBlockingQueue;
 *
 * import org.junit.Test;
 * import static org.junit.Assert.*;
 *
 * import com.notnoop.threadedtc.Conductor;
 *
 * public class Example {
 *
 *     @Test
 *     public void test() {
 *         final Conductor c = new Conductor();
 *         final ArrayBlockingQueue<Integer> buf = new ArrayBlockingQueue<Integer>(1);
 *
 *         c.thread("producer", new TCRunnable() {
 *             public void run() {
 *                 buf.put(42);
 *                 buf.put(17);
 *                 assertEquals(1, c.beat());
 *             }
 *         });
 *
 *         c.thread("consumer", new TCRunnable() {
 *             public void run() {
 *                 c.waitForBeat(1);
 *                 assertEquals(42, (int)buf.take());
 *                 assertEquals(17, (int)buf.take());
 *             }
 *         });
 *
 *         c.waitTillFinished();
 *         assertTrue(buf.isEmpty());
 *     }
 * }
 * </pre>
 *
 * <p>
 * When the test shown is run, it will create one thread named <em>producer</em> and another named
 * <em>consumer</em>. The producer thread will eventually execute the code passed as a by-name
 * parameter to <code>thread("producer")</code>:
 * </p>
 *
 * <pre>
 * buf.put(42);
 * buf.put(17);
 * assertEquals(1, c.beat());
 * </pre>
 *
 * Similarly, the consumer thread will eventually execute the code passed as a by-name parameter
 * to <code>thread("consumer")</code>:
 * </p>
 *
 * <pre>
 * waitForBeat(1);
 * assertEquals(42, (int)buf.take());
 * assertEquals(17, (int)buf.take());
 * </pre>
 *
 * <p>
 * The <code>thread</code> calls create the threads and starts them, but they will not immediately
 * execute the by-name parameter passed to them. They will first block, waiting for the <code>Conductor</code>
 * to give them a green light to proceed.
 * </p>
 *
 * <p>
 * The next call in the test is <code>whenFinished</code>. This method will first call <code>conduct</code> on
 * the <code>Conductor</code>, which will wait until all threads that were created (in this case, producer and consumer) are
 * at the "starting line", <em>i.e.</em>, they have all started and are blocked, waiting on the green light.
 * The <code>conduct</code> method will then give these threads the green light and they will
 * all start executing their blocks concurrently.
 * </p>
 *
 * <p>
 * When the threads are given the green light, the beat is 0. The first thing the producer thread does is put 42 in
 * into the queue. As the queue is empty at this point, this succeeds. The producer thread next attempts to put a 17
 * into the queue, but because the queue has size 1, this can't succeed until the consumer thread has read the 42
 * from the queue. This hasn't happened yet, so producer blocks. Meanwhile, the consumer thread's first act is to
 * call <code>waitForBeat(1)</code>. Because the beat starts out at 0, this call will block the consumer thread.
 * As a result, once the producer thread has executed <code>buf put 17</code> and the consumer thread has executed
 * <code>waitForBeat(1)</code>, both threads will be blocked.
 * </p>
 *
 * <p>
 * The <code>Conductor</code> maintains a clock that wakes up periodically and checks to see if all threads
 * participating in the multi-threaded scenario (in this case, producer and consumer) are blocked. If so, it
 * increments the beat. Thus sometime later the beat will be incremented, from 0 to 1. Because consumer was
 * waiting for beat 1, it will wake up (<em>i.e.</em>, the <code>waitForBeat(1)</code> call will return) and
 * execute the next line of code in its block, <code>buf.take should be (42)</code>. This will succeed, because
 * the producer thread had previously (during beat 0) put 42 into the queue. This act will also make
 * producer runnable again, because it was blocked on the second <code>put</code>, which was waiting for another
 * thread to read that 42.
 * </p>
 *
 * <p>
 * Now both threads are unblocked and able to execute their next statement. The order is
 * non-deterministic, and can even be simultaneous if running on multiple cores. If the <code>consumer</code> thread
 * happens to execute <code>buf.take should be (17)</code> first, it will block (<code>buf.take</code> will not return), because the queue is
 * at that point empty. At some point later, the producer thread will execute <code>buf put 17</code>, which will
 * unblock the consumer thread. Again both threads will be runnable and the order non-deterministic and
 * possibly simulataneous. The producer thread may charge ahead and run its next statement, <code>beat should be (1)</code>.
 * This will succeed because the beat is indeed 1 at this point. As this is the last statement in the producer's block,
 * the producer thread will exit normally (it won't throw an exception). At some point later the consumer thread will
 * be allowed to complete its last statement, the <code>buf.take</code> call will return 17. The consumer thread will
 * execute <code>17 should be (17)</code>. This will succeed and as this was the last statement in its block, the consumer will return
 * normally.
 * </p>
 *
 * <p>
 * If either the producer or consumer thread had completed abruptbly with an exception, the <code>conduct</code> method
 * (which was called by <code>whenFinished</code>) would have completed abruptly with an exception to indicate the test
 * failed. However, since both threads returned normally, <code>conduct</code> will return. Because <code>conduct</code> doesn't
 * throw an exception, <code>whenFinished</code> will execute the block of code passed as a by-name parameter to it: <code>buf should be ('empty)</code>.
 * This will succeed, because the queue is indeed empty at this point. The <code>whenFinished</code> method will then return, and
 * because the <code>whenFinished</code> call was the last statement in the test and it didn't throw an exception, the test completes successfully.
 * </p>
 *
 * <p>
 * This test tests <code>ArrayBlockingQueue</code>, to make sure it works as expected. If there were a bug in <code>ArrayBlockingQueue</code>
 * such as a <code>put</code> called on a full queue didn't block, but instead overwrote the previous value, this test would detect
 * it. However, if there were a bug in <code>ArrayBlockingQueue</code> such that a call to <code>take</code> called on an empty queue
 * never blocked and always returned 0, this test might not detect it. The reason is that whether the consumer thread will ever call
 * <code>take</code> on an empty queue during this test is non-deterministic. It depends on how the threads get scheduled during beat 1.
 * What is deterministic in this test, because the consumer thread blocks during beat 0, is that the producer thread will definitely
 * attempt to write to a full queue. To make sure the other scenario is tested, you'd need a different test:
 * </p>
 *
 * <pre>
 * test("calling take on an empty queue blocks the consumer thread") {
 *
 *   val conductor = new Conductor
 *   import conductor._
 *
 *   val buf = new ArrayBlockingQueue[Int](1)
 *
 *   thread("producer") {
 *     waitForBeat(1)
 *     buf put 42
 *     buf put 17
 *   }
 *
 *   thread("consumer") {
 *     buf.take should be (42)
 *     buf.take should be (17)
 *     beat should be (1)
 *   }
 *
 *   whenFinished {
 *     buf should be ('empty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * In this test, the producer thread will block, waiting for beat 1. The consumer thread will invoke <code>buf.take</code>
 * as its first act. This will block, because the queue is empty. Because both threads are blocked, the <code>Conductor</code>
 * will at some point later increment the beat to 1. This will awaken the producer thread. It will return from its
 * <code>waitForBeat(1)</code> call and execute <code>buf put 42</code>. This will unblock the consumer thread, which will
 * take the 42, and so on.
 * </p>
 *
 * <p>
 * The problem that <code>Conductor</code> is designed to address is the difficulty, caused by the non-deterministic nature
 * of thread scheduling, of testing classes, traits, and libraries that are intended to be used by multiple threads.
 * If you just create a test in which one thread reads from an <code>ArrayBlockingQueue</code> and
 * another writes to it, you can't be sure that you have tested all possible interleavings of threads, no matter
 * how many times you run the test. The purpose of <code>Conductor</code>
 * is to enable you to write tests with deterministic interleavings of threads. If you write one test for each possible
 * interleaving of threads, then you can be sure you have all the scenarios tested. The two tests shown here, for example,
 * ensure that both the scenario in which a producer thread tries to write to a full queue and the scenario in which a
 * consumer thread tries to take from an empty queue are tested.
 * </p>
 *
 * <p>
 * Class <code>Conductor</code> was inspired by the
 * <a href="http://www.cs.umd.edu/projects/PL/multithreadedtc/">MultithreadedTC project</a>,
 * created by Bill Pugh and Nat Ayewah of the University of Maryland.
 * </p>
 *
 */
public final class Conductor {

    /**
     * The metronome used to coordinate between threads.
     * This clock is advanced by the clock thread.
     * The clock will not advance if it is frozen.
     */
    private final Clock clock = new Clock();

    /////////////////////// thread management start //////////////////////////////

    // place all threads in a new thread group
    private final ThreadGroup threadGroup = new ThreadGroup("Orchestra");

    // all the threads in this test
    // This need not be volatile, because it is initialized with one object and
    // that stays forever. Because it is final, it
    private final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<Thread>();

    // Used to keep track of what names have been created so far, so that
    // it can be enforced that the names are unique.
    private final CopyOnWriteArrayList<String> threadNames = new CopyOnWriteArrayList<String>();

    // the main test thread
    private final Thread mainThread = Thread.currentThread();

    /**
     * Creates a new thread that will execute the specified function.
     *
     * <p>
     * The name of the thread will be of the form Conductor-Thread-N, where N is some integer.
     * </p>
     *
     * <p>
     * This method may be safely called by any thread.
     * </p>
     *
     * @param fun the function to be executed by the newly created thread
     * @return the newly created thread
     */
    public Thread thread(Runnable fun) {
        return thread("Conductor-Thread-" + threads.size(), fun);
    }

    public Thread thread(TCRunnable fun) {
        return thread(wrapRunnable(fun));
    }

    /**
     * Creates a new thread with the specified name that will execute the specified function.
     *
     * <p>
     * This method may be safely called by any thread.
     * </p>
     *
     * @param name the name of the newly created thread
     * @param fun the function to be executed by the newly created thread
     * @return the newly created thread
     */
    public Thread thread(String name, Runnable fun) {
        // TODO: Better exceptions
        switch (currentState.get()) {
        case TEST_FINISHED:
//            throw new NotAllowedException(Resources("threadCalledAfterConductingHasCompleted"), getStackDepth("Conductor.scala", "thread"))
            throw new NotAllowedException("threadCalledAfterConductingHasCompleted");
        default:
            if (threadNames.contains(name))
//                throw new NotAllowedException(Resources("cantRegisterThreadsWithSameName", name), getStackDepth("Conductor.scala", "thread"))
                throw new NotAllowedException("cantRegisterThreadsWithSameName: " + name);

            Thread thread = new TestThread(name, fun);
            threads.add(thread);
            threadNames.add(name);
            thread.start();
            return thread;
        }
    }

    public Thread thread(String name, TCRunnable fun) {
        return thread(name, wrapRunnable(fun));
    }

    public Thread getThread(String name) {
        int index = threadNames.indexOf(name);
        if (index != -1)
            return threads.get(index);
        else
            return null;
    }

    // The reason that the thread is started immediately, is so that nested threads
    // will start immediately, without requiring the user to explicitly start() them.
    // Also, so that the thread method can return a Thread object.

    /*
     * A test thread runs the given function.
     * It only does so after it is given permission to do so by the main thread.
     * The main thread grants permission after it receives notication that
     * all test threads are ready to go.
     */
    private class TestThread extends Thread {
        final Runnable runnable;

        public TestThread(String name, Runnable runnable) {
            super(threadGroup, name);
            this.runnable = runnable;

            // Indicate a TestThread has been created that has not yet started running
            testThreadsStartingCounter.increment();
        }

        @Override
        public void run() {
            try {
                // Indicate to the TestThreadsStartingCounter that one more thread is ready to go
                testThreadsStartingCounter.decrement();

                // wait for the main thread to say its ok to go.
                greenLightForTestThreads.await();

                // go
                runnable.run();
            } catch (Throwable t) {
                if (firstExceptionThrown.isEmpty()) {
                    // The mainThread is likely joined to some test thread, so it needs to be awakened. If it
                    // is joined to this thread, it will wake up shortly because this thread is about to die
                    // by returning. If it is joined to a different thread, then it needs to be interrupted,
                    // but this thread can't interrupt it, because then there's a race condition if it is
                    // actually joined to this thread, between join returning because this thread returns
                    // or join throwing an InterruptedException. So here just offer the throwable to
                    // the firstExceptionThrown queue and return. Only the first will be accepted by the queue.
                    // ThreadDeath exceptions that arise from being stopped will not go in because the queue
                    // is already full. The clock thread checks the firestExceptionThrown queue each cycle, and
                    // if it finds it is non-empty, it stops any live thread.
                    firstExceptionThrown.offer(t);
                }
            }
        }
    }

    /**
     * A BlockingQueue containing the first exception that occured
     * in test threads, or that was thrown by the clock thread.
     */
    private ArrayBlockingQueue<Throwable> firstExceptionThrown = new ArrayBlockingQueue<Throwable>(1);

    // Won't write one that takes clockPeriod and timeout for 1.0. For now people
    // can just call conduct(a, b) directly followed by the code they want to run
    // afterwords. See if anyone asks for a whenFinished(a, b) {}
    /**
     * Invokes <code>conduct</code> and after <code>conduct</code> method returns,
     * if <code>conduct</code> returns normally (<em>i.e.</em>, without throwing
     * an exception), invokes the passed function.
     *
     * <p>
     * If <code>conduct</code> completes abruptly with an exception, this method
     * will complete abruptly with the same exception and not execute the passed
     * function.
     * </p>
     *
     * <p>
     * This method must be called by the thread that instantiated this <code>Conductor</code>,
     * and that same thread will invoke <code>conduct</code> and, if it returns noramlly, execute
     * the passed function.
     * </p>
     *
     * <p>
     * Because <code>whenFinished</code> invokes <code>conduct</code>, it can only be invoked
     * once on a <code>Conductor</code> instance. As a result, if you need to pass a block of
     * code to <code>whenFinished</code> it should be the last statement of your test. If you
     * don't have a block of code that needs to be run once all the threads have finished
     * successfully, then you can simply invoke <code>conduct</code> and never invoke
     * <code>whenFinished</code>.
     * </p>
     *
     * @param fun the function to execute after <code>conduct</code> call returns
     * @throws NotAllowedException if the calling thread is not the thread that
     *   instantiated this <code>Conductor</code>, or if <code>conduct</code> has already
     *    been invoked on this conductor.
     */
    public void whenFinished(Runnable fun) {
        if (Thread.currentThread() != mainThread)
            throw new NotAllowedException("whenFinishedCanOnlyBeCalledByMainThread");
//            throw new NotAllowedException(Resources("whenFinishedCanOnlyBeCalledByMainThread"), getStackDepth("Conductor.scala", "whenFinished"))

        if (conductingHasBegun())
            throw new NotAllowedException("cannotInvokeWhenFinishedAfterConduct");
//            throw new NotAllowedException(Resources("cannotInvokeWhenFinishedAfterConduct"), getStackDepth("Conductor.scala", "whenFinished"))

        this.conduct();

        fun.run();
    }

    public void whenFinished(TCRunnable fun) {
        whenFinished(wrapRunnable(fun));
    }

    public void waitTillFinished() {
        whenFinished(new Runnable() { public void run() {} });
    }

    /**
     * Blocks the current thread until the thread beat reaches the
     * specified value, at which point the current thread will be unblocked.
     *
     * @param beat the tick value to wait for
     * @throws NotAllowedException if the a <code>beat</code> less than or equal to zero is passed
     */
    public void waitForBeat(int beat) {
        if (beat == 0)
            throw new NotAllowedException("cannotWaitForBeatZero");
//            throw new NotAllowedException(Resources("cannotWaitForBeatZero"), getStackDepth("Conductor.scala", "waitForBeat"))
        if (beat < 0)
            throw new NotAllowedException("cannotWaitForNevativeBeat");
//            throw new NotAllowedException(Resources("cannotWaitForNegativeBeat"), getStackDepth("Conductor.scala", "waitForBeat"))
        clock.waitForBeat(beat);
    }

    /**
     * The current value of the thread clock.
     *
     * @return the current beat value
     */
    public int beat() {
        return clock.currentBeat();
    }

    /**
     * Executes the passed function with the <code>Conductor</code> <em>frozen</em> so that it
     * won't advance the clock.
     *
     * <p>
     * While the <code>Conductor</code> is frozen, the beat will not advance. Once the
     * passed function has completed executing, the <code>Conductor</code> will be unfrozen
     * so that the beat will advance when all threads are blocked, as normal.
     * </p>
     *
     * @param fun the function to execute while the <code>Conductor</code> is frozen.
     */
    public void withConductorFrozen(Runnable fun) {
        clock.withClockFrozen(fun);
    }

    public void withConductorFrozen(TCRunnable fun) {
        withConductorFrozen(wrapRunnable(fun));
    }

    /**
     * Indicates whether the conductor has been frozen.
     *
     * <p>
     * Note: The only way a thread
     * can freeze the conductor is by calling <code>withConductorFrozen</code>.
     * </p>
     */
    public boolean isConductorFrozen() {
        return clock.isFrozen();
    }

    private TestThreadsStartingCounter testThreadsStartingCounter = new TestThreadsStartingCounter();

    /**
     * Keeps the test threads from executing their bodies until the main thread
     * allows them to.
     */
    private CountDownLatch greenLightForTestThreads = new CountDownLatch(1);

    /**
     * Conducts a multithreaded test with a default clock period of 10 milliseconds
     * and default run limit of 5 seconds.
     */
    public void conduct() {
        int DefaultClockPeriod = 10; // milliseconds
        int DefaultRunLimit = 5; // seconds
        conduct(DefaultClockPeriod, DefaultRunLimit);
    }

    private AtomicReference<ConductorState> currentState
        = new AtomicReference<ConductorState>(ConductorState.SETUP);

    /**
     * Indicates whether either of the two overloaded <code>conduct</code> methods
     * have been invoked.
     *
     * <p>
     * This method returns true if either <code>conduct</code> method has been invoked. The
     * <code>conduct</code> method may have returned or not. (In other words, a <code>true</code>
     * result from this method does not mean the <code>conduct</code> method has returned,
     * just that it was already been invoked and,therefore, the multi-threaded scenario it
     * conducts has definitely begun.)
     * </p>
     */
    public boolean conductingHasBegun() {
        return currentState.get().testWasStarted;
    }

    /**
     * Conducts a multithreaded test with the specified clock period (in milliseconds)
     * and timeout (in seconds).
     *
     * <p>
     * A <code>Conductor</code> instance maintains an internal clock, which will wake up
     * periodically and check to see if it should advance the beat, abort the test, or go back to sleep.
     * It sleeps <code>clockPeriod</code> milliseconds each time. It will abort the test
     * if either deadlock is suspected or the beat has not advanced for the number of
     * seconds specified as <code>timeout</code>. Suspected deadlock will be declared if
     * for some number of consecutive clock cycles, all test threads are in the <code>BLOCKED</code> or
     * <code>WAITING</code> states and none of them are waiting for a beat.
     * </p>
     *
     * @param clockPeriod The period (in ms) the clock will sleep each time it sleeps
     * @param timeout The maximum allowed time between successive advances of the beat. If this time
     *    is exceeded, the Conductor will abort the test.
     * @throws Throwable The first error or exception that is thrown by one of the test threads, or
     *    a <code>TestFailedException</code> if the test was aborted due to a timeout or suspected deadlock.
     */
    public void conduct(int clockPeriod, int timeout) {
        if (clockPeriod <= 0)
            throw new NotAllowedException("cannotPassNonPositiveClockPeriod");
//            throw new NotAllowedException(Resources("cannotPassNonPositiveClockPeriod", clockPeriod.toString), getStackDepth("Conductor.scala", "conduct"))
        if (timeout <= 0)
            throw new NotAllowedException("cannotPassNonPositiveTimeout");
//            throw new NotAllowedException(Resources("cannotPassNonPositiveTimeout", timeout.toString), getStackDepth("Conductor.scala", "conduct"))

        // if the test was started already, explode
        // otherwise, change state to TestStarted
        if (conductingHasBegun())
            throw new NotAllowedException("cannotCallConductTwice");
//            throw new NotAllowedException(Resources("cannotCallConductTwice"), getStackDepth("Conductor.scala", "conduct"))
        else
            currentState.set(ConductorState.TEST_STARTED);

            // wait until all threads are definitely ready to go
            try {
                testThreadsStartingCounter.waitUntilAllTestThreadsHaveStarted();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // release the latch, allowing all threads to start
            // wait for all the test threads to start before starting the clock
            greenLightForTestThreads.countDown();

            // start the clock thread
            ClockThread clockThread = new ClockThread(clockPeriod, timeout);
            clockThread.start();

            // wait until all threads have ended
            waitForThreads();

            // change state to test finished
            currentState.set(ConductorState.TEST_FINISHED);

            if (!firstExceptionThrown.isEmpty()) {
                Throwable throwable = firstExceptionThrown.peek();
                if (throwable instanceof RuntimeException)
                    throw (RuntimeException)throwable;
                else if (throwable instanceof Error)
                    throw (Error)throwable;
                else
                    throw new RuntimeException(throwable);
            }
    }

    /**
     * Wait for all of the test case threads to complete, or for one
     * of the threads to throw an exception, or for the clock thread to
     * interrupt this (main) thread of execution. When the clock thread
     * or other threads fail, the error is placed in the shared error array
     * and thrown by this method.
     *
     * @param threads List of all the test case threads and the clock thread
     */
    // Explain how we understand it works: if the thread that's been joined already dies with an exception
    // that will go into errors, and this thread that called join will return. If the thread that's been joined returns and doesn't
    // die, that means all went well, and join will return and it can loop to the next one.
    // There should be no race condition between the last thread being waited on by join, it dies, join
    // returns, and after that the error gets into the errors. Because if you look in run() in the
    // thread inside createTestThread, the signaling error happens in a catch Throwable block before the thread
    // returns.
    private void waitForThreads() {
        boolean interrupted = false;
        while(!interrupted && areAnyThreadsAlive(threadGroup)) {
            for (Thread t : getThreads(threadGroup)) {
            if (!interrupted && t.isAlive() && firstExceptionThrown.isEmpty())
                try {
                    t.join();
                } catch (InterruptedException e) {
                    // main thread will be interrupted if a timeout occurs, deadlock is suspected,
                    // or a test thread completes abruptly with an exception. Just loop here, because
                    // firstExceptionThrown should be non-empty after InterruptedException is caught, and
                    // if not, then I don't know how it got interrupted, but just keep looping.
                    interrupted = true;
                }
            }
        }
    }



    /**
     * A Clock manages the current beat in a Conductor.
     * Several duties stem from that responsibility.
     *
     * The clock will:
     *
     * <ol>
     * <li>Block a thread until the tick has reached a particular time.</li>
     * <li>Report the current time</li>
     * <li>Run operations with the clock frozen.</li>
     * </ol>
     */
    private class Clock {

        // clock starts at time 0
        private int currentTime = 0;

        // methods in Clock that access or modify the private instance vars of this
        // Clock are synchronized on the object referenced from lock
        private Object lock = new Object();

        /**
         * Read locks are acquired when clock is frozen and must be
         * released before the clock can advance in a advance(). (In a
         * ReentrantReadWriteLock, multiple threads can hold the read lock (and these
         * threads might read the value of currentTime (the currentBeat method), or just execute a
         * function with the clock frozen (the withClockFrozen method). The write lock
         * of a ReentrantReadWriteLock is exclusive, so only one can hold it, and it
         * can't be held if there are a thread or threads holding the read lock. This
         * is why the clock can't advance during a withClockFrozen, because the read
         * lock is grabbed before the function is executed in withClockFrozen, thus
         * advance will not be able to acquire the write lock to update currentTime
         * until after withClockFrozen has released the read lock (and there are no other
         * threads holding a read lock or the write lock).
         */
        private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        private int highestBeatBeingWaitedOn = 0;

        /**
         * Advance the current beat. In order to do so, the clock will wait
         * until it has become unfrozen.
         *
         * All threads waiting for the clock to advance (they would have been put in the lock
         * object's wait set by invoking the waitForBeat method) will be notified after the advance.
         *
         * Only the clock thread should be calling this.
         *
         * If the clock has been frozen by a thread, then that thread will own the readLock. Write
         * lock can only be acquired when there are no readers, so ticks won't progress while someone
         * has the clock frozen. Other methods also grab the read lock, like time (which gets
         * the current beat.)
         */
        void advance() {
            synchronized(lock) {
                rwLock.writeLock().lock();
                currentTime += 1;
                rwLock.writeLock().unlock();
                lock.notifyAll();
            }
        }

        /**
         * The current beat.
         */
        int currentBeat() {
            synchronized(lock) {
                rwLock.readLock().lock();
                try {
                    return currentTime;
                } finally {
                    rwLock.readLock().unlock();
                }
            }
        }

        /**
         * When wait for beat is called, the current thread will block until
         * the given beat is reached by the clock.
         */
        void waitForBeat(int beat) {
            synchronized (lock) {
                if (beat > highestBeatBeingWaitedOn)
                    highestBeatBeingWaitedOn = beat;
                while (currentBeat() < beat) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        // TODO: this is probably fine, but check JCIP about InterEx again
                        throw new AssertionError(e);
                        // Actually I"m not sure. Maybe should reset the interupted status
                    }
                }
            }
        }

        // The reason there's no race condition between calling currentBeat in the while and calling
        // lock.wait() later (between that) and some other thread incrementing the beat and doing
        // a notify that this thread would miss (which it would want to know about if that's the
        // new time that it's waiting for) is because both this and the currentBeat method are synchronized
        // on the lock.

        /**
         * Returns true if any thread is waiting for a beat in the future (greater than the current beat)
         */
        boolean isAnyThreadWaitingForABeat() {
            synchronized(lock) {
                return highestBeatBeingWaitedOn > currentTime;
            }
        }

        /**
         * When the clock is frozen, it will not advance even when all threads
         * are blocked. Use this to block the current thread with a time limit,
         * but prevent the clock from advancing due to a waitForBeat(Int) in
         * another thread.
         */
        void withClockFrozen(Runnable fun) {
            rwLock.readLock().lock();
            try {
                fun.run();
            } finally {
                rwLock.readLock().unlock();
            }
        }

        /**
         * Check if the clock has been frozen by any threads.
         */
        boolean isFrozen() {
            return rwLock.getReadLockCount() > 0;
        }
    }

    /**
     * The clock thread is the manager of the multi-threaded scenario.
     * Periodically checks all the test threads and regulates them.
     * If all the threads are blocked and at least one is waiting for a beat,
     * the clock advances to the next beat and all waiting threads are notified.
     * If none of the threads are waiting for a tick or in timed waiting,
     * a deadlock is detected. The clock thread times out if a thread is in runnable
     * or all are blocked and one is in timed waiting for longer than the runLimit.
     *
     * Algorithm in detail:
     *
     * While there are threads alive
     *
     *    If there are threads RUNNING
     *
     *       If they have been running too long
     *
     *          stop the test with a timeout error
     *
     *    else if there are threads waiting for a beat
     *
     *       advance the clock
     *
     *    else if there are threads in TIMED_WAITING
     *
     *       increment the deadlock counter
     *
     *       if the deadlock counter has reached a threshold
     *
     *          stop the test due to potential deadlock
     *
     *    sleep clockPeriod ms
     *
     *
     * @param mainThread The main test thread. This thread will be waiting
     * for all the test threads to finish. It will be interrupted if the
     * ClockThread detects a deadlock or timeout.
     *
     * @param clockPeriod The period (in ms) between checks for the clock
     *
     * @param maxRunTime The limit to run the test in seconds
     */
    private class ClockThread extends Thread {
        private final int clockPeriod;
        private final int maxRunTime;

        public ClockThread(int clockPeriod, int maxRunTime) {
            super("Conductor-Clock");
            this.clockPeriod = clockPeriod;
            this.maxRunTime = maxRunTime;

            // When a test thread throws an exception, the main thread will stop all the other threads,
            // but won't stop the clock thread. This is because the clock thread will simply return after
            // all the other threads have died. Thus the clock thread could last beyond the end of the
            // application, if the clock period was set high. Thus by making the clock thread a daemon
            // thread, it won't keep the application up just because it is still asleep and hasn't noticed
            // yet that all the test threads are gone.

            this.setDaemon(true);
        }

        // used in detecting timeouts
        private long lastProgress = System.currentTimeMillis();

        // used in detecting deadlocks
        private int deadlockCount = 0;
        private int MaxDeadlockDetectionsBeforeDeadlock = 50;

        /**
         * Runs the steps described above.
         */
        @SuppressWarnings("deprecation")
        @Override
        public void run() {

            // While there are threads that are not NEW or TERMINATED. (A thread is
            // NEW after it has been instantiated, but run() hasn't been called yet.)
            // So this means there are threads that are RUNNABLE, BLOCKED, WAITING, or
            // TIMED_WAITING. (BLOCKED is waiting for a lock. WAITING is in the wait set.)
            while (areAnyThreadsAlive(threadGroup)) {
                if (!firstExceptionThrown.isEmpty()) {
                    // If any exception has been thrown, stop any live test thread.
                    for (Thread t : getThreads(threadGroup)) {
                        if (t.isAlive())
                            t.stop();
                    }
                }

                // If any threads are in the RUNNABLE state, just check to see if there's been
                // no progress for more than the timeout amount of time. If RUNNABLE threads
                // exist, but the timeout limit has not been reached, then just go
                // back to sleep.
                else if (areAnyThreadsRunning(threadGroup)) {
                    // TODO: Change to runningTooLong
                    if (System.currentTimeMillis() - lastProgress > 1000L * maxRunTime) timeout();
                }

                // No RUNNABLE threads, so if any threads are waiting for a beat, advance
                // the beat.
                else if (clock.isAnyThreadWaitingForABeat()) {
                    clock.advance();
                    deadlockCount = 0;
                    lastProgress = System.currentTimeMillis();
                }
                else if (!areAnyThreadsInTimedWaiting(threadGroup)) {
                    // At this point, no threads are RUNNABLE, None
                    // are waiting for a beat, and none are in TimedWaiting.
                    // If this persists for MaxDeadlockDetectionsBeforeDeadlock,
                    // go ahead and abort.
                    detectDeadlock();
                }
                try {
                    Thread.sleep(clockPeriod);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Threads have been running too long (timeout) if
         * The number of seconds since the last progress are more
         * than the allowed maximum run time.
         */
//        private boolean runningTooLong = ;

        /**
         * Stop the test due to a timeout.
         */
        private void timeout() {
            String errorMessage = "testTimedOut"; // Resources("testTimedOut", maxRunTime.toString)
            // The mainThread is likely joined to some test thread, so wake it up. It will look and
            // notice that the firstExceptionThrown is no longer empty, and will stop all live test threads,
            // then rethrow the rirst exception thrown.
            firstExceptionThrown.offer(new TimeoutError(errorMessage));
            mainThread.interrupt();
        }

        /**
         * Determine if there is a deadlock and if so, stop the test.
         */
        private void detectDeadlock() {
            // Should never get to >= before ==, but just playing it safe
            if (deadlockCount >= MaxDeadlockDetectionsBeforeDeadlock) {
                // val errorMessage = "Apparent Deadlock! Threads waiting 50 clock periods (" + (clockPeriod * 50) + "ms)"
                String errorMessage = "suspectedDeadlock"; // Resources("suspectedDeadlock", MaxDeadlockDetectionsBeforeDeadlock.toString, (clockPeriod * MaxDeadlockDetectionsBeforeDeadlock).toString)
                firstExceptionThrown.offer(new DeadlockSuspectedError(errorMessage));

                // The mainThread is likely joined to some test thread, so wake it up. It will look and
                // notice that the firstExceptionThrown is no longer empty, and will stop all live test threads,
                // then rethrow the rirst exception thrown.
                mainThread.interrupt();
            }
            else deadlockCount += 1;
        }
    }

    /**
     * Base class for the possible states of the Conductor.
     */
    private enum ConductorState {

        /**
         * The initial state of the Conductor.
         * Any calls the thread{ ... } will result in started Threads that quickly block waiting for the
         * main thread to give it the green light.
         * Any call to conduct will start the test.
         */
        SETUP(false, false),

        /**
         * The state of the Conductor while its running.
         * Any calls the thread{ ... } will result in running Threads.
         * Any further call to conduct will result in an exception.
         */
        TEST_STARTED(true, false),

        /**
         * The state of the Conductor after all threads have finished,
         * and the whenFinished method has completed.
         * Any calls the thread{ ... } will result in an exception
         * Any call to conduct will result in an exception.
         */
        TEST_FINISHED(true, true);

        final boolean testWasStarted;
//        final boolean testIsFinished;

        ConductorState(boolean testWasStarted, boolean testIsFinished) {
            this.testWasStarted = testWasStarted;
//            this.testIsFinished = testIsFinished;
        }
    }

}
