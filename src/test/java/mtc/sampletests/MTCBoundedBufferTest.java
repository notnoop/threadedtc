package mtc.sampletests;

import com.notnoop.threadedtc.*;

import static com.notnoop.threadedtc.RunnerConductor.*;

import java.util.concurrent.ArrayBlockingQueue;

import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class MTCBoundedBufferTest
{
    ArrayBlockingQueue<Integer> buf;

    @Before
    public void initialize()
    {
        buf = new ArrayBlockingQueue<Integer>(1);
    }

    @Threaded
    public void threadPutPut() throws InterruptedException
    {
        waitForBeat(1);
        buf.put(42);
        buf.put(17);
    }

    @Threaded
    public void threadTakeTake() throws InterruptedException
    {
        assertTrue(buf.take() == 42);
        assertTrue(buf.take() == 17);
        assertEquals(1, beat());
    }

    @Test
    public void finish()
    {
        assertTrue(buf.isEmpty());
    }
}
