package com.wyj.excel.convert;


import java.util.Date;

/**
 * Created by wyj on 17-9-19.
 */
public class StringToDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String s) {
        if (s == null || "".equals(s.trim())) {
            return null;
        }
        return new Date(Long.valueOf(s));
    }

}
