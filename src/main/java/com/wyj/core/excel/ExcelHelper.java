package com.wyj.core.excel;

import com.wyj.core.excel.annotation.Excel;
import com.wyj.core.excel.annotation.Nesting;
import com.wyj.core.util.ReflexUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * Created by wyj on 17-9-25.
 */
public class ExcelHelper {

	private static Map<Class, List<Field>> fieldMap;

	private static Map<Class, List<ExcelField>> excelFieldMap;

	static {
		fieldMap = new ConcurrentHashMap<>(32);
		excelFieldMap = new ConcurrentHashMap<>(32);
	}

	// 不支持嵌套
	@Deprecated
	public static List<Field> getDeclaredFieldsOrder(Class clazz) {
		if (fieldMap.get(clazz) != null) {
			return fieldMap.get(clazz);
		}
		Field[] declaredFields = ReflexUtils.getAllField(clazz);
		List<Field> fieldList = new ArrayList<>();
		for (Field field : declaredFields) {
			if (field.isAnnotationPresent(Excel.class)) {
				fieldList.add(field);
			}
		}

		List<Field> collect = fieldList.stream().sorted((f1, f2) -> {
			Excel excel1 = f1.getAnnotation(Excel.class);
			Excel excel2 = f2.getAnnotation(Excel.class);
			return excel1.order() - excel2.order();
		}).collect(toList());

		fieldMap.put(clazz, collect);
		return collect;
	}

	// 支持嵌套
	public static List<ExcelField> getExcelFields(Class clazz) {
		// 查询缓存
		if (excelFieldMap.get(clazz) != null) {
			return excelFieldMap.get(clazz);
		}

		List<ExcelField> excelFields = getExcelFields(clazz, true, new HashSet<>());

		// 添加缓存
		excelFieldMap.put(clazz, excelFields);
		return excelFields;
	}

	private static List<ExcelField> getExcelFields(Class clazz, boolean isOrder, Set<Class> existClass) {
		// 防止死循环(不是一个拓扑结构,存在循环嵌套)
		if (existClass.contains(clazz)) {
			return new ArrayList<>();
		}
		existClass.add(clazz);

		List<ExcelField> fieldList = new ArrayList<>();

		Field[] declaredFields = ReflexUtils.getAllField(clazz);
		for (Field field : declaredFields) {

			if (field.isAnnotationPresent(Nesting.class)) {
				// 嵌套调用
				List<ExcelField> nestingFields = getExcelFields(field.getType(), false, existClass);

				nestingFields = nestingFields.stream()
						.map((excelField -> ExcelFieldBuilder.newInstance(field)
								.addField(excelField.getRoute())
								.build()))
						.collect(toList());
				fieldList.addAll(nestingFields);
			} else if (field.isAnnotationPresent(Excel.class)) {
				fieldList.add(ExcelFieldBuilder.newInstance(field).build());
			}
		}

		// 是否排序
		if (isOrder) {
			fieldList = fieldList.stream().sorted((f1, f2) -> {
				Excel excel1 = f1.getAnnotation(Excel.class);
				Excel excel2 = f2.getAnnotation(Excel.class);
				return excel1.order() - excel2.order();
			}).collect(toList());
		}
		return fieldList;
	}

}
