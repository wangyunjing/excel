package com.wyj.core.excel;

import com.wyj.core.excel.annotation.Excel;
import com.wyj.core.util.ReflexUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by wyj on 17-9-25.
 */
public class ExcelHelper {

	private static Map<Class, List<Field>> fieldMap;

	static {
		fieldMap = new ConcurrentHashMap<>(32);
	}

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
		}).collect(Collectors.toList());

		fieldMap.put(clazz, collect);
		return collect;
	}

}
