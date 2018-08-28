package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class ShortToStringConverter implements Converter<Short, String> {

	@Override
	public String convert(Short s) {
		if (s == null) {
			return null;
		}
		return s.toString();
	}

}
