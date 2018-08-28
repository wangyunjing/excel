package com.wyj.excel.convert;

/**
 * Created by wyj on 17-9-19.
 */
public interface Converter<S, T> {
	T convert(S s);
}
