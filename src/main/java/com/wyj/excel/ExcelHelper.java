package com.wyj.excel;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.annotation.Nesting;
import com.wyj.excel.convert.ConverterService;
import com.wyj.excel.util.ReflexUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * Created by wyj on 17-9-25.
 */
public class ExcelHelper {

	private static Map<Class, List<ExcelField>> excelFieldMap;

	static {
		excelFieldMap = new ConcurrentHashMap<>(32);
	}

	// 支持嵌套
	public static List<ExcelField> getExcelFields(Class clazz, ConverterService converterService) {
		// 查询缓存
		if (excelFieldMap.get(clazz) != null) {
			return excelFieldMap.get(clazz);
		}

		List<ExcelField> excelFields = getExcelFields(clazz, converterService, true, new HashSet<>());

		// 添加缓存
		excelFieldMap.put(clazz, excelFields);
		return excelFields;
	}

	private static List<ExcelField> getExcelFields(Class clazz, ConverterService converterService,
												   boolean isOrder, Set<Class> existClass) {
		// 防止死循环(不是一个拓扑结构,存在循环嵌套)
		if (existClass.contains(clazz)) {
			return new ArrayList<>();
		}
		existClass.add(clazz);

		List<ExcelField> fieldList = new ArrayList<>();

		Field[] declaredFields = ReflexUtils.getAllField(clazz);
		for (Field field : declaredFields) {

			if (field.isAnnotationPresent(Nesting.class)) {
				// 嵌套调用 (无需每一步都排序，只要最后一次排序就可以)
				List<ExcelField> nestingFields = getExcelFields(field.getType(), converterService, false, existClass);

				nestingFields = nestingFields.stream()
						.map(excelField -> ExcelField.Builder.newInstance(field)
								.addField(excelField.getRoute())
								.setConverterService(excelField.getConverterService())
								.setExcel(excelField.getExcel())
								.build())
						.collect(toList());
				fieldList.addAll(nestingFields);
			} else if (field.isAnnotationPresent(Excel.class)) {
				fieldList.add(ExcelField.Builder.newInstance(field)
						.setConverterService(converterService)
						.setExcel(field.getAnnotation(Excel.class))
						.build());
			}
		}

		// 是否排序
		if (isOrder) {
			fieldList = fieldList.stream().sorted((f1, f2) -> {
				Excel excel1 = f1.getExcel();
				Excel excel2 = f2.getExcel();
				return excel1.order() - excel2.order();
			}).collect(toList());
		}
		return fieldList;
	}

}
