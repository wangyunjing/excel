package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class StringToLongConverter implements Converter<String, Long> {

	@Override
	public Long convert(String s) {
		if (s == null) {
			return null;
		}
		try {
			return Long.parseLong(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
