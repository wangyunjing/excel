package com.example.convert;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

	@Override
	public Boolean convert(String s) {
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		if ("1".equals(s) || "on".equals(s) || "true".equals(s)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

}
