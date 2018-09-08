package com.wyj.excel.convert;

import java.util.Date;

public class DateToLongConverter implements Converter<Date, Long> {
    @Override
    public Long convert(Date date) {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }
}
