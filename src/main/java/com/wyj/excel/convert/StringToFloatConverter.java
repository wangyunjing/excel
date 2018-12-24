package com.wyj.excel.convert;

public class StringToFloatConverter implements Converter<String, Float> {
    @Override
    public Float convert(String s) {
        if (s == null || "".equals(s.trim())) {
            return null;
        }
        return Float.valueOf(s);
    }
}
