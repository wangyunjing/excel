package com.wyj.excel;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.convert.ConverterService;
import com.wyj.excel.util.ReflexUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wyj on 17-12-21.
 */
public class ExcelField {

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
    public Object get(Object object) {
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
     * 通过get方法获取值
     *
     * @param object
     * @param targetClass 如果targetClass==String.class，并且没有对应的类型转换器，则默认使用toString方法
     *                    主要原因：减少类的数量
     * @param <T>
     * @return
     */
    public <T> T get(Object object, Class<T> targetClass) {
        object = get(object);
        if (object == null || targetClass.isAssignableFrom(object.getClass())) {
            return (T) object;
        }
        if (converterService.isSupport(object.getClass(), targetClass)) {
            return converterService.convert(object.getClass(), targetClass, object);
        }
        if (targetClass == String.class) {
            return (T) targetClass.toString();
        }
        throw new RuntimeException("没有对应的转换器, sourceClass=" + object.getClass().getName() + ", targetClass=" + targetClass.getName());
    }

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

        if (targetClass.isAssignableFrom(sourceClass)) {
            ReflexUtils.setFieldValue(instance.getClass(), instance, field.getName(), targetClass.cast(value));
            return;
        }
        Object convert = converterService.convert(sourceClass, targetClass, value);
        ReflexUtils.setFieldValue(instance.getClass(), instance, field.getName(), convert);
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
