package com.wyj.excel.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class DoubleToStringConverter implements Converter<Double, String> {

	@Override
	public String convert(Double s) {
		if (s == null) {
			return null;
		}
		return s.toString();
	}

}
