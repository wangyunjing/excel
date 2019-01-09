package com.wyj.excel.function;

/**
 * 需要结合{@link com.wyj.excel.util.Try}使用
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
}
