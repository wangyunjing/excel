package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class LongToStringConverter implements Converter<Long, String> {

	@Override
	public String convert(Long s) {
		if (s == null) {
			return null;
		}
		return s.toString();
	}

}
