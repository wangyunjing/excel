package com.example.convert;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToLongConverter implements Converter<String, Long> {

	@Override
	public Long convert(String s) {
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		try {
			return Long.parseLong(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
