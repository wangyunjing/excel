package com.wyj.excel;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.convert.Converter;
import com.wyj.excel.convert.ConverterService;
import com.wyj.excel.convert.DateFormattingHandler;
import com.wyj.excel.util.ReflexUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by wyj on 17-12-21.
 */
public class ExcelField {

    private static final Object DEFAULT_OBJECT = new Object();

    private static final Map<Class<? extends Converter>, Converter> SPECIFIC_CONVERTER = new HashMap<>();

    private Excel excel;

    private ConverterService converterService;

    private Field[] route;

    public ExcelField(Excel excel, ConverterService converterService, Field[] route) {
        this.excel = excel;
        this.converterService = converterService == null ? ConverterService.create() : converterService;
        this.route = route;
    }

    public Excel getExcel() {
        return excel;
    }

    public ConverterService getConverterService() {
        return converterService;
    }

    public Field[] getRoute() {
        return route;
    }

    private Field getLastField() {
        if (route.length == 0) {
            return null;
        }
        return route[route.length - 1];
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        Field lastField = getLastField();
        if (lastField == null) {
            return null;
        }
        return lastField.getAnnotation(annotation);
    }

    // 通过get方法获取值
    private Object get(Object object) {
        Class sourceClazz = object.getClass();

        for (Field field : getRoute()) {
            object = ReflexUtils.getFieldValue(sourceClazz, object, field.getName());
            if (object == null) {
                return null;
            }
            sourceClazz = field.getType();
        }
        return object;
    }

    /**
     * 导出时使用
     * 通过get方法获取值
     */
    public <T> T get(Object instance, Class<T> targetClass) {
        if (instance == null){
            return null;
        }
        instance = get(instance);

        Class sourceClass = instance.getClass();
        // 指定转换器
        Object result = specificConverter(excel.exportConverter(), instance);
        // 时间格式化
        if (result == DEFAULT_OBJECT) {
            result = dateHandler(sourceClass, targetClass, instance);
        }
        // 全局默认转换器
        if (result == DEFAULT_OBJECT && converterService.isSupport(instance.getClass(), targetClass)) {
            result = converterService.convert(instance.getClass(), targetClass, instance);
        }
        // targetClass所表示的类或接口与sourceClass所表示的类或接口是否相同，或是否是其超类或超接口。如果是则返回 true；否则返回 false
        if (result == DEFAULT_OBJECT && targetClass.isAssignableFrom(instance.getClass())) {
            result = targetClass.cast(instance);
        }
        // 如果targetClass==String.class，并且没有对应的类型转换器，则默认使用toString方法。主要原因：减少类的数量
        if (result == DEFAULT_OBJECT && targetClass == String.class) {
            result =  instance.toString();
        }
        if (result != DEFAULT_OBJECT) {
            return (T) result;
        }
        throw new RuntimeException("没有对应的转换器, sourceClass=" + instance.getClass().getName() + ", targetClass=" + targetClass.getName());
    }

    /**
     * 导入时使用
     * 通过get方法获取值
     * 通过set方法设置值
     */
    public void set(Object instance, Object value) {
        if (value == null) {
            return;
        }
        if (excel.emptyToNull() && "".equals(value)) {
            return;
        }
        if (route == null || route.length == 0) {
            return;
        }
        for (int i = 0; i < route.length - 1; i++) {
            Field field = route[i];
            Object fieldValue = ReflexUtils.getFieldValue(instance.getClass(), instance, field.getName());

            if (fieldValue == null) {
                try {
                    fieldValue = field.getType().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("创建" + field.getType() + "实例失败");
                }
                ReflexUtils.setFieldValue(instance.getClass(), instance, field.getName(), fieldValue);
            }
            instance = fieldValue;
        }
        Field field = route[route.length - 1];

        Class sourceClass = value.getClass();
        Class targetClass = field.getType();
        // 指定转换器
        Object convert = specificConverter(excel.importConverter(), value);
        // 时间格式化
        if (convert == DEFAULT_OBJECT) {
            convert = dateHandler(sourceClass, targetClass, value);
        }
        // 全局默认转换器
        if (convert == DEFAULT_OBJECT && converterService.isSupport(instance.getClass(), targetClass)) {
            convert = converterService.convert(sourceClass, targetClass, value);
        }
        // targetClass所表示的类或接口与sourceClass所表示的类或接口是否相同，或是否是其超类或超接口。如果是则返回 true；否则返回 false
        if (convert == DEFAULT_OBJECT && targetClass.isAssignableFrom(sourceClass)) {
            convert = targetClass.cast(value);
        }
        if (convert != DEFAULT_OBJECT) {
            ReflexUtils.setFieldValue(instance.getClass(), instance, field.getName(), convert);
            return;
        }
        throw new RuntimeException("没有对应的转换器, sourceClass=" + instance.getClass().getName() + ", targetClass=" + targetClass.getName());
    }

    // 时间格式化处理
    private Object dateHandler(Class sourceClass, Class targetClass, Object source) {
        if (!sourceClass.isAssignableFrom(source.getClass())) {
            return DEFAULT_OBJECT;
        }
        Object result = DEFAULT_OBJECT;
        Converter<?, ?> converter = converterService.getConverter(sourceClass, targetClass);
        if (converter != null && converter instanceof DateFormattingHandler && !"".equals(excel.dateFormat())) {
            DateFormattingHandler handler = (DateFormattingHandler) converter;
            if (handler.isSupport(sourceClass, targetClass)) {
                result = handler.handle(source, excel.dateFormat());
            }
        }
        return result;
    }

    // 使用指定的转换器
    private Object specificConverter(Class<? extends Converter> converterClass, Object value) {
        if (converterClass != Converter.class) {
            Converter converter = SPECIFIC_CONVERTER.get(converterClass);
            if (converter == null) {
                synchronized (SPECIFIC_CONVERTER) {
                    try {
                        converter = SPECIFIC_CONVERTER.get(converterClass);
                        if (converter == null) {
                            converter = converterClass.newInstance();
                            SPECIFIC_CONVERTER.put(converterClass, converter);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("创建" + converterClass.getSimpleName() + "实例失败");
                    }
                }
            }
            return converter.convert(value);
        }
        return DEFAULT_OBJECT;
    }

    public static class Builder {

        private Excel excel;
        private ConverterService converterService;
        private List<Field> list = new ArrayList<>();

        private Builder() {
        }

        private Builder(List<Field> list) {
            this.list.addAll(list);
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public static Builder newInstance(List<Field> list) {
            return new Builder(list);
        }

        public static Builder newInstance(Field... fields) {
            return new Builder(Arrays.asList(fields));
        }

        public Builder addField(Field field) {
            list.add(field);
            return this;
        }

        public Builder addField(Field... fields) {
            if (fields == null || fields.length == 0) {
                return this;
            }
            list.addAll(Arrays.asList(fields));
            return this;
        }

        public Builder addField(List<Field> list) {
            if (list == null || list.size() == 0) {
                return this;
            }
            list.addAll(list);
            return this;
        }

        public Builder setConverterService(ConverterService converterService) {
            this.converterService = converterService;
            return this;
        }

        public Builder setExcel(Excel excel) {
            this.excel = excel;
            return this;
        }

        public ExcelField build() {
            if (list == null || list.size() == 0) {
                return null;
            }
            return new ExcelField(excel, converterService == null ? ConverterService.getDefaultInstance() : converterService,
                    list.toArray(new Field[list.size()]));
        }
    }
}
