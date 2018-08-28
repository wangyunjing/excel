package com.wyj.excel.convert;

public class StringToByteConverter implements Converter<String, Byte> {
    @Override
    public Byte convert(String s) {
        if (s == null) {
            return null;
        }
        return Byte.valueOf(s);
    }
}
