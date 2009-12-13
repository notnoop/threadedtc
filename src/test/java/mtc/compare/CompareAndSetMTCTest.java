package mtc.compare;

import com.notnoop.threadedtc.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class CompareAndSetMTCTest
{

    AtomicInteger ai;

    @Before
    public void initialize()
    {
        ai = new AtomicInteger(1);
    }

    @Threaded
    public void thread1()
    {
        while (!ai.compareAndSet(2, 3)) Thread.yield();
    }

    @Threaded
    public void thread2()
    {
        assertTrue(ai.compareAndSet(1, 2));
    }

    @Test
    public void finish()
    {
        assertEquals(ai.get(), 3);
    }
}
