package com.wyj.excel.convert;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToDateConverter implements Converter<String, Date> {

	private String format = "yyyy-MM-dd HH:mm:ss";

	@Override
	public Date convert(String s) {
		if (s == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(s);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
