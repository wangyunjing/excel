package com.wyj.excel.convert;

public class ByteToStringConverter implements Converter<Byte, String> {

    @Override
    public String convert(Byte aByte) {
        if (aByte == null) {
            return null;
        }
        return aByte.toString();
    }
}
