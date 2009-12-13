package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import static com.notnoop.threadedtc.RunnerConductor.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestSanityMetronomeOrder
{
    String s;

    @Before
    public void initialize()
    {
        s = "";
    }

    @Threaded
    public void thread1()
    {
        waitForBeat(1);
        s += "A";

        waitForBeat(3);
        s += "C";

        waitForBeat(6);
        s += "F";
    }

    @Threaded
    public void thread2()
    {
        waitForBeat(2);
        s += "B";

        waitForBeat(5);
        s += "E";

        waitForBeat(8);
        s += "H";
    }

    @Threaded
    public void thread3()
    {
        waitForBeat(4);
        s += "D";

        waitForBeat(7);
        s += "G";

        waitForBeat(9);
        s += "I";
    }

    @Test
    public void finish()
    {
        assertEquals("Threads were not called in correct order",
                s, "ABCDEFGHI");
    }
}
