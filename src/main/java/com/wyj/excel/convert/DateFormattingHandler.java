package com.wyj.excel.convert;

public interface DateFormattingHandler<T, R> {

    boolean isSupport(Class source, Class target);

    R handle(T value, String pattern);
}
