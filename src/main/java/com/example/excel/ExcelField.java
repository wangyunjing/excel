package com.example.excel;

import com.example.convert.ConverterService;
import com.example.excel.annotation.Excel;
import com.example.util.ReflexUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

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
}
