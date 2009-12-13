package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import static com.notnoop.threadedtc.RunnerConductor.*;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
*/
@RunWith(TCRunner.class)
public class TestSanityWaitForTickAdvancesWhenTestsAreBlocked
{
    CountDownLatch c;

    @Before
    public void initialize() {
        c = new CountDownLatch(3);
    }

    @Threaded
    public void thread1() throws InterruptedException {
        c.countDown();
        c.await();
    }

    @Threaded
    public void thread2() throws InterruptedException {
        c.countDown();
        c.await();
    }

    @Threaded
    public void thread3() {
        waitForBeat(1);
        assertEquals(1, c.getCount());
        waitForBeat(2); // advances quickly
        assertEquals(1, c.getCount());
        c.countDown();
    }

    @Test
    public void finish() {
        assertEquals(0, c.getCount());
    }
}
