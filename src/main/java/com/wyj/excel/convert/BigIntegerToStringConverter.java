package com.wyj.excel.convert;

import java.math.BigInteger;

public class BigIntegerToStringConverter implements Converter<BigInteger, String> {
    @Override
    public String convert(BigInteger bigInteger) {
        if (bigInteger == null) {
            return null;
        }
        return bigInteger.toString();
    }

}
