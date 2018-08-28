package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class IntegerToStringConverter implements Converter<Integer, String> {

	@Override
	public String convert(Integer s) {
		if (s == null) {
			return null;
		}
		return s.toString();
	}

}
