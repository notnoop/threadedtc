package mtc.compare;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class ThreadControlMTCTest
{

    AtomicInteger ai;

    @Before
    public void initialize()
    {
        ai = new AtomicInteger(0);
    }

    @Threaded
    public void thread1()
    {
        assertTrue(ai.compareAndSet(0, 1)); // S1
        waitForBeat(3);
        assertEquals(ai.get(), 3);          // S4
    }

    @Threaded
    public void thread2()
    {
        waitForBeat(1);
        assertEquals(1, beat());
        assertTrue(ai.compareAndSet(1, 2)); // S2
        waitForBeat(3);
        assertEquals(3, beat());
        assertEquals(ai.get(), 3);          // S4
    }

    @Threaded
    public void thread3()
    {
        waitForBeat(2);
        assertEquals(2, beat());
        assertTrue(ai.compareAndSet(2, 3)); // S3
    }

    @Test
    public void test()
    {
    }
}
