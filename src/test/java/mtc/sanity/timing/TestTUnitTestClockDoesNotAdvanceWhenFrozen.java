package mtc.sanity.timing;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestTUnitTestClockDoesNotAdvanceWhenFrozen
{
    String s;

    @Before
    public void initialize()
    {
        s = "A";
    }

    @Threaded("first")
    public void thread1() throws InterruptedException
    {
        RunnerConductor.withConductorFrozen(new TCRunnable() {
            public void run() throws Exception {
                Thread.sleep(200);
                assertEquals("Clock advanced while thread was sleeping", s, "A");                
            }
        });        
    }

    @Threaded("second")
    public void thread2()
    {
        waitForBeat(1);
        s = "B";
    }

    @Test
    public void finish()
    {
        assertEquals(s, "B");
    }
}
