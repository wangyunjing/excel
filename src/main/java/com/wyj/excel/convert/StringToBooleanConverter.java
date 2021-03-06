package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

	@Override
	public Boolean convert(String s) {
		if (s == null || "".equals(s.trim())) {
			return null;
		}
		if ("1".equals(s) || "on".equals(s) || "true".equals(s)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

}
