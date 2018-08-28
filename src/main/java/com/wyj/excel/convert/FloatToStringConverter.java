package com.wyj.excel.convert;

public class FloatToStringConverter implements Converter<Float, String>{
    @Override
    public String convert(Float aFloat) {
        if (aFloat == null) {
            return null;
        }
        return aFloat.toString();
    }

}
