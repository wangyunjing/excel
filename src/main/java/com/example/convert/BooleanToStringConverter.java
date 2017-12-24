package com.example.convert;


/**
 * Created by wyj on 17-9-19.
 */
public class BooleanToStringConverter implements Converter<Boolean, String> {

	@Override
	public String convert(Boolean s) {
		if (s == null) {
			return null;
		}
		return s.toString();
	}

}
