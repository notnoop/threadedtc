package threadedtc.examples;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.*;
import org.junit.runner.RunWith;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;

@RunWith(TCRunner.class)
public class JunitExample {
    ArrayBlockingQueue<Integer> buf = new ArrayBlockingQueue<Integer>(1);

    @Threaded
    public void producer() throws Exception {
        buf.put(42);
        buf.put(17);
        Assert.assertEquals(1, beat());
    }

    @Threaded
    public void consumer() throws Exception {
        waitForBeat(1);
        Assert.assertEquals(42, (int)buf.take());
        Assert.assertEquals(17, (int)buf.take());
    }

    @Test
    public void fullQueueShouldBlock() {
        Assert.assertTrue(buf.isEmpty());
    }
}
