package com.notnoop.threadedtc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class TCRunner extends BlockJUnit4ClassRunner {

    public TCRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    private int timesToRepeatSetup(FrameworkMethod method) {
        MultiThreadedTest t = method.getAnnotation(MultiThreadedTest.class);
        return (t == null) ? 1 : t.times();
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        int times = timesToRepeatSetup(method);
        return new TCStatement(method, test, times);
    }

    private static final class TCStatement extends InvokeMethod {
        private final Object target;
        private final int times;
        boolean ignored;

        public TCStatement(FrameworkMethod testMethod, Object target, int count) {
            super(testMethod, target);
            this.target = target;
            this.times = count;
            ignored = testMethod.getAnnotation(Ignore.class) != null;
        }

        private String name(String n1, String n2) {
            return n1.isEmpty() ? n2 : n1;
        }

        private void prepareAndConduct() {
            Conductor c = new Conductor();
            RunnerConductor.conductor = c;

            Class<?> clazz = target.getClass();
            for (final Method method : clazz.getMethods()) {
                if (method.getAnnotation(Threaded.class) == null)
                    continue;

                Threaded threaded = method.getAnnotation(Threaded.class);
                String name = name(threaded.value(), method.getName());
                c.thread(name, new TCRunnable() {

                    public void run() throws Throwable {
                        try {
                            method.invoke(target);
                        } catch (InvocationTargetException e) {
                            if (e.getCause() != null)
                                throw e.getCause();
                            else
                                throw e;
                        }
                    }
                });
            }
            c.conduct();
        }

        @Override
        public void evaluate() throws Throwable {
            if (!ignored) {
                for (int i = 0; i < times; ++i) {
                    prepareAndConduct();
                }
            }
            super.evaluate();
        }
    }
}
