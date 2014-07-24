ThreadedTC Testing Framework
===================================


ThreadedTC is a library for testing multi-threaded programs and controllers.
It provides a best-effort approach to detecting deadlocks and timeouts.
It integrates well with testing frameworks, especially JUnit.

The code class of the library is `Conductor`. A Conductor conducts a
multi-threaded scenario by maintaining a clock of "beats." Beats are numbered
starting with 0. You can ask a Conductor to run threads that interact with the
class, trait, or library (the subject) you want to test. A thread can call the
Conductor's waitForBeat method, which will cause the thread to block until
that beat has been reached. The Conductor will advance the beat only when all
threads participating in the test are blocked. By tying the timing of thread
activities to specific beats, you can write tests for concurrent systems that
have deterministic interleavings of threads.

The library is based on the works of [`scalatest`](http://www.scalatest.org/).
The concurrency utilities classes are based on CMU's
[MultiThreadedTC](http://code.google.com/p/multithreadedtc/).

Use of Conductor
================================

`Conductor` can be used into ways: as an independent class, or within a
custom Junit 4 Runner.

For illustration, I'll examine that `ArrayBlockingQueue` actually blocks the
putter thread when calling put on a full queue.

As an Independent Driver Class
-----------------------------------
Conductor can be used independently from any external tool, without any
required integration with the testing tool.  The test would look like:

    import com.notnoop.threadedtc.*;

    public class Example {

        @Test
        public void fullQueueShouldBlock() {
            final Conductor c = new Conductor();
            final ArrayBlockingQueue<Integer> buf =
                new ArrayBlockingQueue<Integer>(1);

            c.thread("producer", new TCRunnable() {
                public void run() throws Exception {
                    buf.put(42);
                    buf.put(17);
                    assertEquals(1, c.beat());
                }
            });

            c.thread("consumer", new TCRunnable() {
                public void run() throws Exception {
                    c.waitForBeat(1);
                    assertEquals(42, (int)buf.take());
                    assertEquals(17, (int)buf.take());
                }
            });

            c.waitTillFinished();
            Assert.assertTrue(buf.isEmpty());
        }
    }

This has the benefit of having all relevant threading logic in one method,
but has an excess of inner anonymous classes.

Used With Custom JUnit 4 Runner
------------------------------------
This library integrates well with JUnit 4.  It provides a concise mechanism
to write a new test.  Each thread could be separated into different methods:

    import com.notnoop.threadedtc.*;
    import static com.notnoop.threadedtc.RunnerConductor.*;

    @RunWith(TCRunner.class)
    public class JunitExample {
        ArrayBlockingQueue<Integer> buf = new ArrayBlockingQueue<Integer>(1);

        @Threaded
        public void producer() throws Exception {
            buf.put(42);
            buf.put(17);
            assertEquals(1, beat());
        }

        @Threaded
        public void consumer() throws Exception {
            waitForBeat(1);
            assertEquals(42, (int)buf.take());
            assertEquals(17, (int)buf.take());
        }

        @Test
        public void fullQueueShouldBlock() {
            assertTrue(buf.isEmpty());
        }
    }


Bonus: Interaction with Straw-Man Proposal
---------------------------------------------

The straw-Man Proposal allows for more compact syntax.  The Straw-Man
Closure Proposal permits a more concise syntax for initiating some inner
classes.  The sample example would look like:

    @Test
    public void fullQueueShouldBlock() {
        final Conductor c = new Conductor();
        final ArrayBlockingQueue<Integer> buf =
            new ArrayBlockingQueue<Integer>(1);

        c.thread("producer", #() {
            buf.put(42);
            buf.put(17);
            assertEquals(1, c.beat());
        });

        c.thread("consumer", #() {
            c.waitForBeat(1);
            assertEquals(42, (int)buf.take());
            assertEquals(17, (int)buf.take());
        });

        c.waitTillFinished();
        Assert.assertTrue(buf.isEmpty());
    }
