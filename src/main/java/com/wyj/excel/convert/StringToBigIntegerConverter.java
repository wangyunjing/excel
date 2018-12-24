package com.wyj.excel.convert;

import java.math.BigInteger;

public class StringToBigIntegerConverter implements Converter<String, BigInteger> {
    @Override
    public BigInteger convert(String s) {
        if (s == null || "".equals(s.trim())) {
            return null;
        }
        try {
            return new BigInteger(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
