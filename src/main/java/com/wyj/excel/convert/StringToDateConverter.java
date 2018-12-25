package com.wyj.excel.convert;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToDateConverter implements Converter<String, Date>, DateFormattingHandler<String, Date> {

    @Override
    public boolean isSupport(Class source, Class target) {
        return String.class.equals(source) && Date.class.equals(target);
    }

    @Override
    public Date handle(String value, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Date convert(String s) {
        if (s == null || "".equals(s.trim())) {
            return null;
        }
        return new Date(Long.valueOf(s));
    }

}
