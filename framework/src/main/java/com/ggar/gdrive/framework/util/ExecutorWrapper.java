package com.ggar.gdrive.framework.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ExecutorWrapper {

    public static <T> T execute(Callable<T> callable) {
        return ExecutorWrapper.<T>execute(callable, Exception::printStackTrace);
    }

    public static <T> T execute(Callable<T> callable, Consumer<Exception> consumer) {
        T result = null;
        try {
            result = callable.call();
        } catch (Exception e) {
            consumer.accept(e);
            throw new RuntimeException(e);
        }
        return result;
    }
}
