package com.example.excel;

import com.example.convert.ConverterService;
import com.example.util.ReflexUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Created by wyj on 17-12-21.
 */
public class ExcelField {

	private Field[] route;

	public ExcelField(Field[] route) {
		this.route = route;
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
	public <T> T get(Object object, Class<T> targetClazz) {
		Class sourceClazz = object.getClass();

		for (Field field : getRoute()) {
			object = ReflexUtils.getFieldValue(sourceClazz, object, field);
			if (object == null) {
				return null;
			}
			sourceClazz = field.getType();
		}

		if (sourceClazz == targetClazz) {
			return (T) object;
		}

		if (ConverterService.isSupport(sourceClazz, targetClazz)) {
			Optional<T> optional = ConverterService.convert(sourceClazz, targetClazz, object);
			return optional.orElse(null);
		}

		if (targetClazz == String.class) {
			return (T) (object.toString());
		}

		return null;
	}

	public void set(Object instance, Object value) {
		if (route == null || route.length == 0) {
			return;
		}
		for (int i = 0; i < route.length - 1; i++) {
			Field field = route[i];
			Object fieldValue = ReflexUtils.getFieldValue(instance.getClass(), instance, field);

			if (fieldValue == null) {
				try {
					fieldValue = field.getType().newInstance();
				} catch (Exception e) {
					throw new RuntimeException("创建" + field.getType() + "实例失败");
				}
				ReflexUtils.setFieldValue(instance.getClass(), instance, field, fieldValue);
			}

			instance = fieldValue;
		}

		Field field = route[route.length - 1];

		Class sourceClass = value.getClass();
		Class targetClass = field.getType();

		if (sourceClass == targetClass) {
			ReflexUtils.setFieldValue(instance.getClass(), instance, field, value);
			return;
		}

		if (ConverterService.isSupport(sourceClass, targetClass)) {
			Optional<?> optional = ConverterService.convert(sourceClass, targetClass, value);
			Object arg = optional.orElse(null);

			ReflexUtils.setFieldValue(instance.getClass(), instance, field, arg);
			return;
		}
		throw new RuntimeException("不支持" + sourceClass + "转换成" + targetClass);
	}
}
