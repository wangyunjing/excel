package com.example.convert;

import java.math.BigDecimal;

public class StringToBigDecimalConverter implements Converter<String, BigDecimal> {
    @Override
    public BigDecimal convert(String s) {
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
