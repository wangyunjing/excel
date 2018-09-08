package com.wyj.excel.convert;

import java.util.Date;

/**
 * Created by wyj on 17-9-25.
 */
public class DateToStringConverter implements Converter<Date, String> {

    @Override
    public String convert(Date date) {
        if (date == null) {
            return null;
        }
        return String.valueOf(date.getTime());
    }

}
