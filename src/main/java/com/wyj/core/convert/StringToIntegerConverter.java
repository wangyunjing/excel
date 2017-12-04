package com.wyj.core.convert;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToIntegerConverter implements Converter<String, Integer> {

	@Override
	public Integer convert(String s) {
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
