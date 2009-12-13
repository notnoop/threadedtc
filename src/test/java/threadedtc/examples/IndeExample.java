package threadedtc.examples;

import java.util.concurrent.ArrayBlockingQueue;

import org.junit.*;

import com.notnoop.threadedtc.*;

public class IndeExample {

    @Test
    public void fullQueueShouldBlock() {
        final Conductor c = new Conductor();
        final ArrayBlockingQueue<Integer> buf =
            new ArrayBlockingQueue<Integer>(1);

        c.thread("producer", new TCRunnable() {
            public void run() throws Exception {
                buf.put(42);
                buf.put(17);
                Assert.assertEquals(1, c.beat());
            }
        });

        c.thread("consumer", new TCRunnable() {
            public void run() throws Exception {
                c.waitForBeat(1);
                Assert.assertEquals(42, (int)buf.take());
                Assert.assertEquals(17, (int)buf.take());
            }
        });

        c.waitTillFinished();
        Assert.assertTrue(buf.isEmpty());
    }
}
