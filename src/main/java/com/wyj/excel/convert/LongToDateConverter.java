package com.wyj.excel.convert;

import java.util.Date;

public class LongToDateConverter implements Converter<Long, Date> {
    @Override
    public Date convert(Long aLong) {
        if (aLong == null) {
            return null;
        }
        return new Date(aLong);
    }
}
