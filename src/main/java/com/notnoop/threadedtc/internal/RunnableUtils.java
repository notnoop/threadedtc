package com.notnoop.threadedtc.internal;

import com.notnoop.threadedtc.TCRunnable;

public class RunnableUtils {
    private RunnableUtils() { }

    public static Runnable wrapRunnable(final TCRunnable runnable) {
        return new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
