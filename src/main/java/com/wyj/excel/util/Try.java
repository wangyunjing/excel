package com.wyj.excel.util;

import com.wyj.excel.function.CheckedConsumer;
import com.wyj.excel.function.CheckedFunction;
import com.wyj.excel.function.CheckedRunnable;

import java.util.function.Consumer;
import java.util.function.Function;

public class Try {

    public static Runnable of(CheckedRunnable checkedRunnable) {
        return () -> {
            try {
                checkedRunnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Consumer<T> of(CheckedConsumer<T> checkedConsumer) {
        return t -> {
            try {
                checkedConsumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, R> Function<T, R> of(CheckedFunction<T, R> checkedFunction) {
        return t -> {
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
