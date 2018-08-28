package com.example.convert;

import java.math.BigDecimal;

public class BigDecimalToStringConverter implements Converter<BigDecimal, String> {
    @Override
    public String convert(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        return bigDecimal.toPlainString();
    }
}
