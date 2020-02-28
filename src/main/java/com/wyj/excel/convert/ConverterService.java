package com.wyj.excel.convert;

import com.wyj.excel.util.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wyj on 17-9-19.
 */
public class ConverterService {

    private static final Map<ConverterPair, Converter<?, ?>> DEFAULT_MAP = new HashMap<>();

    private static class ConverterServiceHolder {
        public static final ConverterService DEFAULT_CONVERTER_SERVICE = ConverterService.create();
    }

    static {
        addConverter(DEFAULT_MAP, String.class, Date.class, new StringToDateConverter());
        addConverter(DEFAULT_MAP, String.class, Boolean.class, new StringToBooleanConverter());
        addConverter(DEFAULT_MAP, String.class, boolean.class, new StringToBooleanConverter());
        addConverter(DEFAULT_MAP, String.class, Byte.class, new StringToByteConverter());
        addConverter(DEFAULT_MAP, String.class, byte.class, new StringToByteConverter());
        addConverter(DEFAULT_MAP, String.class, Short.class, new StringToShortConverter());
        addConverter(DEFAULT_MAP, String.class, short.class, new StringToShortConverter());
        addConverter(DEFAULT_MAP, String.class, Integer.class, new StringToIntegerConverter());
        addConverter(DEFAULT_MAP, String.class, int.class, new StringToIntegerConverter());
        addConverter(DEFAULT_MAP, String.class, Long.class, new StringToLongConverter());
        addConverter(DEFAULT_MAP, String.class, long.class, new StringToLongConverter());
        addConverter(DEFAULT_MAP, String.class, Float.class, new StringToFloatConverter());
        addConverter(DEFAULT_MAP, String.class, float.class, new StringToFloatConverter());
        addConverter(DEFAULT_MAP, String.class, Double.class, new StringToDoubleConverter());
        addConverter(DEFAULT_MAP, String.class, double.class, new StringToDoubleConverter());
        addConverter(DEFAULT_MAP, String.class, BigInteger.class, new StringToBigIntegerConverter());
        addConverter(DEFAULT_MAP, String.class, BigDecimal.class, new StringToBigDecimalConverter());

        addConverter(DEFAULT_MAP, Date.class, String.class, new DateToStringConverter());
        addConverter(DEFAULT_MAP, Boolean.class, String.class, new BooleanToStringConverter());
        addConverter(DEFAULT_MAP, boolean.class, String.class, new BooleanToStringConverter());
        addConverter(DEFAULT_MAP, Byte.class, String.class, new ByteToStringConverter());
        addConverter(DEFAULT_MAP, byte.class, String.class, new ByteToStringConverter());
        addConverter(DEFAULT_MAP, Short.class, String.class, new ShortToStringConverter());
        addConverter(DEFAULT_MAP, short.class, String.class, new ShortToStringConverter());
        addConverter(DEFAULT_MAP, Integer.class, String.class, new IntegerToStringConverter());
        addConverter(DEFAULT_MAP, int.class, String.class, new IntegerToStringConverter());
        addConverter(DEFAULT_MAP, Long.class, String.class, new LongToStringConverter());
        addConverter(DEFAULT_MAP, long.class, String.class, new LongToStringConverter());
        addConverter(DEFAULT_MAP, Float.class, String.class, new FloatToStringConverter());
        addConverter(DEFAULT_MAP, float.class, String.class, new FloatToStringConverter());
        addConverter(DEFAULT_MAP, Double.class, String.class, new DoubleToStringConverter());
        addConverter(DEFAULT_MAP, double.class, String.class, new DoubleToStringConverter());
        addConverter(DEFAULT_MAP, BigInteger.class, String.class, new BigIntegerToStringConverter());
        addConverter(DEFAULT_MAP, BigDecimal.class, String.class, new BigDecimalToStringConverter());

        addConverter(DEFAULT_MAP, Date.class, Long.class, new DateToLongConverter());
        addConverter(DEFAULT_MAP, Date.class, long.class, new DateToLongConverter());
        addConverter(DEFAULT_MAP, Long.class, Date.class, new LongToDateConverter());
        addConverter(DEFAULT_MAP, long.class, Date.class, new LongToDateConverter());
    }

    private Map<ConverterPair, Converter<?, ?>> map = new HashMap<>();

    private ConverterService() {
        map.putAll(DEFAULT_MAP);
    }

    public static ConverterService create() {
        return new ConverterService();
    }

    public static ConverterService create(ConverterService converterService) {
        ConverterService copyConverterService = new ConverterService();
        converterService.map.putAll(converterService.map);
        return copyConverterService;
    }

    public static ConverterService getDefaultInstance() {
        return ConverterServiceHolder.DEFAULT_CONVERTER_SERVICE;
    }

    /**
     * 如果存在相同的转换类型,则会覆盖之前的
     * 如果不存在,则添加该类型转换
     *
     * @param sourceClass
     * @param targetClass
     * @param converter
     */
    public void addConverter(Class sourceClass, Class targetClass, Converter<?, ?> converter) {
        addConverter(map, sourceClass, targetClass, converter);
    }

    private static void addConverter(Map<ConverterPair, Converter<?, ?>> map,
                                     Class sourceClass, Class targetClass, Converter<?, ?> converter) {
        Assert.notNull(sourceClass, "sourceClass不能为空");
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(converter, "converter不能为空");
        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
        map.put(converterPair, converter);
    }

    public boolean isSupport(Class sourceClass, Class targetClass) {
        Assert.notNull(sourceClass, "sourceClass不能为空");
        Assert.notNull(targetClass, "targetClass不能为空");
        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
        return map.get(converterPair) == null ? false : true;
    }

    public Converter<?, ?> getConverter(Class sourceClass, Class targetClass) {
        Assert.notNull(sourceClass, "sourceClass不能为空");
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(sourceClass, "sourceClass不能为空");
        Assert.notNull(targetClass, "targetClass不能为空");
        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
        return map.get(converterPair);
    }

    public <T> T convert(Class sourceClass, Class<T> targetClass, Object source) {
        Assert.notNull(sourceClass, "sourceClass不能为空");
        Assert.notNull(targetClass, "targetClass不能为空");
        Assert.notNull(source, "source不能为空");
        if (!sourceClass.isAssignableFrom(source.getClass())) {
            throw new RuntimeException("sourceClass and source 类型不匹配! sourceClass=" +
                    sourceClass.getTypeName() + "\tsource=" + source.getClass().getTypeName());
        }
        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
        Converter converter = map.get(converterPair);
        if (converter == null) {
            throw new RuntimeException("not support " + sourceClass.getName() + " convert to " + targetClass);
        }
        return (T) converter.convert(sourceClass.cast(source));
    }

    private static class ConverterPair {
        private Class sourceClass;

        private Class targetClass;

        public ConverterPair(Class sourceClass, Class targetClass) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
        }

        public Class getSourceClass() {
            return sourceClass;
        }

        public Class getTargetClass() {
            return targetClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConverterPair that = (ConverterPair) o;

            if (sourceClass != null ? !sourceClass.equals(that.sourceClass) : that.sourceClass != null) return false;
            return targetClass != null ? targetClass.equals(that.targetClass) : that.targetClass == null;
        }

        @Override
        public int hashCode() {
            int result = sourceClass != null ? sourceClass.hashCode() : 0;
            result = 31 * result + (targetClass != null ? targetClass.hashCode() : 0);
            return result;
        }
    }
}
