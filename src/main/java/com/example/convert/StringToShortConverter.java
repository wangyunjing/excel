package com.example.convert;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToShortConverter implements Converter<String, Short> {

	@Override
	public Short convert(String s) {
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		try {
			return Short.parseShort(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
