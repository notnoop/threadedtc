package mtc.compare;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class InterruptBlockedMTCTest
{
    Semaphore s;

    @Before
    public void initialize()
    {
        s = new Semaphore(0);
    }

    @Threaded("1")
    public void thread1()
    {
        try
        {
            s.acquire();
            fail("should throw exception");
        } catch (InterruptedException success)
        {
            assertEquals(1, beat());
        }
    }

    @Threaded
    public void thread2()
    {
        waitForBeat(1);
        getThread("1").interrupt();
    }

    @Test
    public void test()
    {
    }
}
