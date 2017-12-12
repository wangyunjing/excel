package com.wyj.core.convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wyj on 17-9-19.
 */
public class ConverterService {

	public static ConverterService getInstance() {
		return ConverterSupportHolder.converterService;
	}

	public static class ConverterSupportHolder {
		public static ConverterService converterService = new ConverterService();
	}

	private Map<ConverterPair, ArrayList<Class<? extends Converter>>> map = new ConcurrentHashMap<>();


	private ConverterService() {
		this.doAddConvert(String.class, Date.class, StringToDateConverter.class);
		this.doAddConvert(String.class, Integer.class, StringToIntegerConverter.class);
		this.doAddConvert(String.class, Double.class, StringToDoubleConverter.class);
		this.doAddConvert(Date.class, String.class, DateToStringConverter.class);
	}

	public static boolean isSupport(Class sourceClass, Class targetClass) {
		if (sourceClass == null || targetClass == null) {
			return false;
		}
		ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
		ArrayList<Class<? extends Converter>> classes = getInstance().map.get(converterPair);
		return classes == null ? false : true;
	}

	public static <T> Optional<T> convert(Class sourceClass, Class<T> targetClass, Object source) {
		if (!isSupport(sourceClass, targetClass)) {
			throw new RuntimeException("not support " + sourceClass.getName() + " convert to " + targetClass);
		}
		if (sourceClass == null || targetClass == null || source == null) {
			throw new RuntimeException("不能为空");
		}
		if (!source.getClass().equals(sourceClass)) {
			throw new RuntimeException("sourceClass and source 类型不一致!");
		}

		ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);

		ArrayList<Class<? extends Converter>> classes = getInstance().map.get(converterPair);
		Exception exception = null;
		for (Class<? extends Converter> converterClass : classes) {
			try {
				Converter converter = converterClass.newInstance();
				return Optional.ofNullable((T) converter.convert(source));
			} catch (Exception e) {
				exception = e;
			}
		}
		throw new RuntimeException("类型转换出错!", exception);
	}

	/**
	 * 如果存在相同的转换类型,则会把新添加的放入第一个(优先使用);
	 * 如果不存在,则添加该类型转换
	 * @param sourceClass
	 * @param targetClass
	 * @param converter
	 */
	public static void addConvert(Class sourceClass, Class targetClass, Class<? extends Converter> converter) {
		getInstance().doAddConvert(sourceClass, targetClass, converter);
	}

	public synchronized void doAddConvert(Class sourceClass, Class targetClass, Class<? extends Converter> converter) {
		if (sourceClass == null || targetClass == null || converter == null) {
			throw new RuntimeException("不能为空");
		}
		ConverterPair converterPair = new ConverterPair(sourceClass, targetClass);
		ArrayList<Class<? extends Converter>> classes = map.get(converterPair);
		if (classes == null) {
			classes = new ArrayList<>();
		}
		if (classes.contains(converter)) {
			classes.remove(converter);
		}
		classes.add(0, converter);
		map.put(converterPair, classes);
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
