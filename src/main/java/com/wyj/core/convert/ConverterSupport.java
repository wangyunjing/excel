package com.wyj.core.convert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wyj on 17-9-19.
 */
public class ConverterSupport {

	public static ConverterSupport getInstance() {
		return ConverterSupportHolder.converterSupport;
	}

	public static class ConverterSupportHolder {
		public static ConverterSupport converterSupport = new ConverterSupport();
	}

	private Map<ConverterMapKey, Class<? extends Converter>> map = new ConcurrentHashMap<>();


	private ConverterSupport() {
		this.doAddConvert(String.class, Date.class, StringToDateConverter.class);
		this.doAddConvert(String.class, Integer.class, StringToIntegerConverter.class);
		this.doAddConvert(String.class, Double.class, StringToDoubleConverter.class);
		this.doAddConvert(Date.class, String.class, DateToStringConverter.class);
	}

	public static boolean isSupport(Class sourceClass, Class targetClass) {
		if (sourceClass == null || targetClass == null) {
			return false;
		}
		ConverterMapKey converterMapKey = new ConverterMapKey(sourceClass, targetClass);
		Class<? extends Converter> converterClass = getInstance().map.get(converterMapKey);
		return converterClass == null ? false : true;
	}

	public static  <T> Optional<T> convert(Class sourceClass, Class<T> targetClass, Object source) throws
			IllegalAccessException, InstantiationException {
		if (sourceClass == null || targetClass == null || source == null) {
			throw new RuntimeException("不能为空");
		}
		if (!source.getClass().equals(sourceClass)) {
			throw new RuntimeException("sourceClass and source 类型不一致!");
		}
		ConverterMapKey converterMapKey = new ConverterMapKey(sourceClass, targetClass);
		Class<? extends Converter> converterClass = getInstance().map.get(converterMapKey);
		if (converterClass == null) {
			throw new RuntimeException("not support " + sourceClass.getName() + " convert to " + targetClass);
		}
		Converter converter = converterClass.newInstance();
		return Optional.ofNullable((T) converter.convert(source));
	}

	public static void addConvert(Class sourceClass, Class targetClass, Class<? extends Converter> converter) {
		getInstance().doAddConvert(sourceClass, targetClass, converter);
	}

	public void doAddConvert(Class sourceClass, Class targetClass, Class<? extends Converter> converter) {
		if (sourceClass == null || targetClass == null || converter == null) {
			throw new RuntimeException("不能为空");
		}
		ConverterMapKey converterMapKey = new ConverterMapKey(sourceClass, targetClass);
		map.put(converterMapKey, converter);
	}

	private static class ConverterMapKey {
		private Class sourceClass;

		private Class targetClass;

		public ConverterMapKey(Class sourceClass, Class targetClass) {
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

			ConverterMapKey that = (ConverterMapKey) o;

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
