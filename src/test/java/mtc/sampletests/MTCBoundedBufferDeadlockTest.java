package mtc.sampletests;

import com.notnoop.threadedtc.*;
import com.notnoop.threadedtc.exceptions.DeadlockSuspectedError;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Can we implement the Bounded Buffer using CountDownLatch? Nope, this causes a deadlock! But MTC can detect deadlocks. So we'll use the
 * CountDownLatch version to demonstrate MTC's deadlock detection capabilities.
 */
@RunWith(TCRunner.class)
public class MTCBoundedBufferDeadlockTest
{
    ArrayBlockingQueue<Integer> buf;
    CountDownLatch c;

    @Before
    public void initialize()
    {
        buf = new ArrayBlockingQueue<Integer>(1);
        c = new CountDownLatch(1);
    }

    @Threaded
    public void threadPutPut() throws InterruptedException
    {
        buf.put(42);
        buf.put(17);
        c.countDown();
    }

    @Threaded
    public void thread2() throws InterruptedException
    {
        c.await();
        assertEquals(Integer.valueOf(42), buf.take());
        assertEquals(Integer.valueOf(17), buf.take());
    }

    @Test(expected = DeadlockSuspectedError.class)
    public void test()
    {
    }
}
