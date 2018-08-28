package com.wyj.excel.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wyj on 17-9-19.
 */
public class ConverterService {

    private static final Map<ConverterPair, ArrayList<Converter<?, ?>>> DEFAULT_MAP = new HashMap<>();

    static {
        addConvert(DEFAULT_MAP, String.class, Date.class, new StringToDateConverter());
        addConvert(DEFAULT_MAP, String.class, Boolean.class, new StringToBooleanConverter());
        addConvert(DEFAULT_MAP, String.class, boolean.class, new StringToBooleanConverter());
        addConvert(DEFAULT_MAP, String.class, Byte.class, new StringToByteConverter());
        addConvert(DEFAULT_MAP, String.class, byte.class, new StringToByteConverter());
        addConvert(DEFAULT_MAP, String.class, Short.class, new StringToShortConverter());
        addConvert(DEFAULT_MAP, String.class, short.class, new StringToShortConverter());
        addConvert(DEFAULT_MAP, String.class, Integer.class, new StringToIntegerConverter());
        addConvert(DEFAULT_MAP, String.class, int.class, new StringToIntegerConverter());
        addConvert(DEFAULT_MAP, String.class, Long.class, new StringToLongConverter());
        addConvert(DEFAULT_MAP, String.class, long.class, new StringToLongConverter());
        addConvert(DEFAULT_MAP, String.class, Float.class, new StringToFloatConverter());
        addConvert(DEFAULT_MAP, String.class, float.class, new StringToFloatConverter());
        addConvert(DEFAULT_MAP, String.class, Double.class, new StringToDoubleConverter());
        addConvert(DEFAULT_MAP, String.class, double.class, new StringToDoubleConverter());
        addConvert(DEFAULT_MAP, String.class, BigInteger.class, new StringToBigIntegerConverter());
        addConvert(DEFAULT_MAP, String.class, BigDecimal.class, new StringToBigDecimalConverter());

        addConvert(DEFAULT_MAP, Date.class, String.class, new DateToStringConverter());
        addConvert(DEFAULT_MAP, Boolean.class, String.class, new BooleanToStringConverter());
        addConvert(DEFAULT_MAP, boolean.class, String.class, new BooleanToStringConverter());
        addConvert(DEFAULT_MAP, Byte.class, String.class, new ByteToStringConverter());
        addConvert(DEFAULT_MAP, byte.class, String.class, new ByteToStringConverter());
        addConvert(DEFAULT_MAP, Short.class, String.class, new ShortToStringConverter());
        addConvert(DEFAULT_MAP, short.class, String.class, new ShortToStringConverter());
        addConvert(DEFAULT_MAP, Integer.class, String.class, new IntegerToStringConverter());
        addConvert(DEFAULT_MAP, int.class, String.class, new IntegerToStringConverter());
        addConvert(DEFAULT_MAP, Long.class, String.class, new LongToStringConverter());
        addConvert(DEFAULT_MAP, long.class, String.class, new LongToStringConverter());
        addConvert(DEFAULT_MAP, Float.class, String.class, new FloatToStringConverter());
        addConvert(DEFAULT_MAP, float.class, String.class, new FloatToStringConverter());
        addConvert(DEFAULT_MAP, Double.class, String.class, new DoubleToStringConverter());
        addConvert(DEFAULT_MAP, double.class, String.class, new DoubleToStringConverter());
        addConvert(DEFAULT_MAP, BigInteger.class, String.class, new BigIntegerToStringConverter());
        addConvert(DEFAULT_MAP, BigDecimal.class, String.class, new BigDecimalToStringConverter());
    }

    private Map<ConverterPair, ArrayList<Converter<?, ?>>> map = new HashMap<>();

    private ConverterService() {
        map.putAll(DEFAULT_MAP);
    }

    public static ConverterService create() {
        return new ConverterService();
    }

    public static ConverterService create(ConverterService converterService) {
        ConverterService copyConverterService = new ConverterService();
        copyConverterService.map.clear();
        for (Map.Entry<ConverterPair, ArrayList<Converter<?, ?>>> entry : converterService.map.entrySet()) {
            ConverterPair key = entry.getKey();
            ArrayList<Converter<?, ?>> value = entry.getValue();
            ArrayList<Converter<?, ?>> copyValue = new ArrayList<>(value.size());
            copyValue.addAll(value);
            copyConverterService.map.put(key, copyValue);
        }
        return copyConverterService;
    }

    /**
     * 如果存在相同的转换类型,则会把新添加的放入第一个(优先使用);
     * 如果不存在,则添加该类型转换
     *
     * @param sourceClass
     * @param targetClass
     * @param converter
     */
    public void addConvert(Class sourceClass, Class targetClass, Converter<?, ?> converter) {
        addConvert(map, sourceClass, targetClass, converter);
    }

    private static void addConvert(Map<ConverterPair, ArrayList<Converter<?, ?>>> map,
                                   Class sourceClass, Class targetClass, Converter<?, ?> converter) {
        if (sourceClass == null || targetClass == null || converter == null) {
            throw new RuntimeException("不能为空");
        }
        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
        ArrayList<Converter<?, ?>> classes = map.get(converterPair);
        if (classes == null) {
            classes = new ArrayList<>();
        }
        if (classes.contains(converter)) {
            classes.remove(converter);
        }
        classes.add(0, converter);
        map.put(converterPair, classes);
    }

    private boolean isSupport(Class sourceClass, Class targetClass) {
        if (sourceClass == null || targetClass == null) {
            return false;
        }
        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
        ArrayList<Converter<?, ?>> classes = map.get(converterPair);
        return classes == null ? false : true;
    }

    public <T> T convert(Class sourceClass, Class<T> targetClass, Object source) {
        if (!isSupport(sourceClass, targetClass)) {
            throw new RuntimeException("not support " + sourceClass.getName() + " convert to " + targetClass);
        }
        if (sourceClass == null || targetClass == null || source == null) {
            throw new RuntimeException("不能为空");
        }
        if (!sourceClass.isAssignableFrom(source.getClass())) {
            throw new RuntimeException("sourceClass and source 类型不匹配! sourceClass=" +
                    sourceClass.getTypeName() + "\tsource=" + source.getClass().getTypeName());
        }

        ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);

        ArrayList<Converter<?, ?>> converters = map.get(converterPair);
        Exception exception = null;
        for (Converter converter : converters) {
            try {
                return (T) converter.convert(source);
            } catch (Exception e) {
                exception = e;
            }
        }
        throw new RuntimeException("类型转换出错!", exception);
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
