package com.example.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wyj on 17-10-23.
 */
public class BeanUtils {

	public static void assembleExcludeNull(Object source, Object target, String... excludeFields) {
		assemble(source, target, true, excludeFields);
	}

	public static void assembleIncludeNull(Object source, Object target, String... excludeFields) {
		assemble(source, target, false, excludeFields);
	}

	public static void assemble(Object source, Object target, boolean excludeNull, String... excludeFields) {
		Assert.notNull(source);
		Assert.notNull(target);

		List<String> excludeFieldList = new ArrayList<>();
		if (excludeFields != null && excludeFields.length > 0) {
			excludeFieldList = Arrays.asList(excludeFields);
		}

		Class<?> sourceClass = source.getClass();
		Class<?> targetClass = target.getClass();

		Method[] sourceGetMethods = ReflexUtils.getUniqueGetMethod(sourceClass);
		Method[] targetSetMethods = ReflexUtils.getUniqueSetMethod(targetClass);

		for (Method sourceGetMethod : sourceGetMethods) {

			for (Method targetSetMethod : targetSetMethods) {
				String sourceName = ReflexUtils.getFieldNameByMethodName(sourceGetMethod.getName(), 3);
				String targetName = ReflexUtils.getFieldNameByMethodName(targetSetMethod.getName(), 3);

				Class<?> sourceReturnType = sourceGetMethod.getReturnType();
				Class<?> targetParameterType = targetSetMethod.getParameterTypes()[0];

				if (sourceName.equals(targetName) && sourceReturnType.equals(targetParameterType)) {
					// 排除不copy的字段
					if (excludeFieldList.contains(sourceName)) {
						break;
					}

					Object arg = ReflexUtils.invokeMethod(sourceGetMethod, source);

					// 排除null字段的copy
					if (excludeNull == true && arg == null) {
						break;
					}
					ReflexUtils.invokeMethod(targetSetMethod, target, arg);
					break;
				}
			}
		}
	}

}
