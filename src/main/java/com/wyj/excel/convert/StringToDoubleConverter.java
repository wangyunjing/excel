package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class StringToDoubleConverter implements Converter<String, Double> {

	@Override
	public Double convert(String s) {
		if (s == null || "".equals(s.trim())) {
			return null;
		}
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
