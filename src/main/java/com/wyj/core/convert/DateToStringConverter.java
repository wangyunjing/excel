package com.wyj.core.convert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by wyj on 17-9-25.
 */
public class DateToStringConverter implements Converter<Date, String> {


	private String format = "yyyy-MM-dd HH:mm:ss";

	@Override
	public String convert(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		return sdf.format(date);
	}

}
