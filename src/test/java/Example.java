import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Test;
import static org.junit.Assert.*;

import com.notnoop.threadedtc.Conductor;

public class Example {

    @Test
    public void test() {
        final Conductor c = new Conductor();
        final ArrayBlockingQueue<Integer> buf = new ArrayBlockingQueue<Integer>(1);

        c.thread("producer", new Runnable() {
            public void run() {
                try {
                    buf.put(42);
                    buf.put(17);
                    assertEquals(1, c.beat());
                } catch (InterruptedException e) {
                }
            }
        });

        c.thread("consumer", new Runnable() {
            public void run() {
                try {
                    c.waitForBeat(1);
                    assertEquals(42, (int)buf.take());
                    assertEquals(17, (int)buf.take());
                } catch (InterruptedException e) {
                }
            }
        });

        c.whenFinished(new Runnable() {
            public void run() {
                System.out.println(buf);
                assertTrue(buf.isEmpty());
            }
        });
    }
}
