package com.notnoop.threadedtc;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiThreadedTest {
    int times() default 1;
}
