package com.wyj.excel.convert;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wyj on 17-9-25.
 */
public class DateToStringConverter implements Converter<Date, String>, DateFormattingHandler<Date, String> {

    @Override
    public boolean isSupport(Class source, Class target) {
        return Date.class.equals(source) && String.class.equals(target);
    }

    @Override
    public String handle(Date value, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(value);
    }

    @Override
    public String convert(Date date) {
        if (date == null) {
            return null;
        }
        return String.valueOf(date.getTime());
    }

}
