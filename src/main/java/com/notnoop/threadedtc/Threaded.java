package com.notnoop.threadedtc;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Threaded {
    String value() default "";
}
