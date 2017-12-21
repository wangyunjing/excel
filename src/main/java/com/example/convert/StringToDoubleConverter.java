package com.example.convert;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToDoubleConverter implements Converter<String, Double> {

	@Override
	public Double convert(String s) {
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
