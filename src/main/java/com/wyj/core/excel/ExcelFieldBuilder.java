package com.wyj.core.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wyj on 17-12-21.
 */
public class ExcelFieldBuilder {

	private List<Field> list = new ArrayList<>();;

	private ExcelFieldBuilder() {
	}

	private ExcelFieldBuilder(List<Field> list) {
		this.list.addAll(list);
	}

	public static ExcelFieldBuilder newInstance() {
		return new ExcelFieldBuilder();
	}

	public static ExcelFieldBuilder newInstance(List<Field> list) {
		return new ExcelFieldBuilder(list);
	}

	public static ExcelFieldBuilder newInstance(Field... fields) {
		return new ExcelFieldBuilder(Arrays.asList(fields));
	}

	public ExcelFieldBuilder addField(Field field) {
		list.add(field);
		return this;
	}

	public ExcelFieldBuilder addField(Field... fields) {
		if (fields == null || fields.length == 0) {
			return this;
		}
		list.addAll(Arrays.asList(fields));
		return this;
	}

	public ExcelFieldBuilder addField(List<Field> list) {
		if (list == null || list.size() == 0) {
			return this;
		}
		list.addAll(list);
		return this;
	}

	public ExcelField build() {
		if (list == null || list.size() == 0) {
			return null;
		}
		return new ExcelField(list.toArray(new Field[list.size()]));
	}

}
