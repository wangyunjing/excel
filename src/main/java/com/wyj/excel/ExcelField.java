package com.wyj.excel;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.convert.Converter;
import com.wyj.excel.convert.ConverterService;
import com.wyj.excel.convert.DateFormattingHandler;
import com.wyj.excel.util.ReflexUtils;

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

    ConverterService getConverterService() {
        return converterService;
    }

    Field[] getRoute() {
        return route;
    }

    public Excel getExcel() {
        return excel;
    }

    /**
     * 导出时使用
     * 通过get方法获取值
     */
    public String get(Object instance) {
        if (instance == null) {
            return null;
        }
        // 获取具体的实例
        for (Field field : getRoute()) {
            instance = ReflexUtils.getFieldValue(instance.getClass(), instance, field.getName());
            if (instance == null) {
                break;
            }
        }
        Class sourceClass = instance.getClass();
        // 指定转换器
        Object result = specificConverter(excel.exportConverter(), instance);
        // 时间格式化
        result = result == DEFAULT_OBJECT ? dateHandler(sourceClass, String.class, instance) : result;
        // 全局默认转换器
        result = result == DEFAULT_OBJECT && converterService.isSupport(sourceClass, String.class)
                ? converterService.convert(sourceClass, String.class, instance)
                : result;
        // String.class所表示的类或接口与sourceClass所表示的类或接口是否相同，或是否是其超类或超接口。如果是则返回 true；否则返回 false
        result = result == DEFAULT_OBJECT && String.class.isAssignableFrom(sourceClass) ? instance : result;
        // 如果没有对应的类型转换器，则默认使用toString方法。
        return result == DEFAULT_OBJECT ? instance.toString() : (String) result;
    }

    /**
     * 导入时使用
     * 通过get方法获取值
     * 通过set方法设置值
     */
    public void set(Object instance, String value) {
        if (value == null || excel.emptyToNull() && "".equals(value) || route == null || route.length == 0) {
            return;
        }
        // 获取具体的实例和字段
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

        Class targetClass = field.getType();
        // 指定转换器
        Object result = specificConverter(excel.importConverter(), value);
        // 时间格式化
        result = result == DEFAULT_OBJECT ? dateHandler(String.class, targetClass, value) : result;
        // 全局默认转换器
        result = result == DEFAULT_OBJECT && converterService.isSupport(String.class, targetClass)
                ? converterService.convert(String.class, targetClass, value)
                : result;
        // targetClass所表示的类或接口与String.class所表示的类或接口是否相同，或是否是其超类或超接口。如果是则返回 true；否则返回 false
        result = result == DEFAULT_OBJECT && targetClass.isAssignableFrom(String.class) ? targetClass.cast(value) : result;
        if (result != DEFAULT_OBJECT) {
            ReflexUtils.setFieldValue(instance.getClass(), instance, field.getName(), result);
            return;
        }
        throw new RuntimeException("没有对应的转换器, sourceClass=String.class, targetClass=" + targetClass.getName());
    }

    // 时间格式化处理
    private Object dateHandler(Class sourceClass, Class targetClass, Object source) {
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
