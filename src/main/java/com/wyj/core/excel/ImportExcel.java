package com.wyj.core.excel;

import com.wyj.core.convert.ConverterSupport;
import com.wyj.core.excel.annotation.Excel;
import com.wyj.core.excel.exception.ExcelImportException;
import com.wyj.core.util.ClassUtils;
import com.wyj.core.util.ReflexUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

/**
 * Created by wyj on 17-12-4.
 */

/**
 * 第0行 表示 标题
 * 其余行 表示 数据
 *
 * @param <T>
 */
public class ImportExcel<T> {

	private Workbook workbook;
	private Sheet sheet;
	private Row title;

	private Class<T> clazz;

	private List<Field> list = new ArrayList<>();

	private ImportExcel(InputStream inputStream, Class<T> clazz, boolean is03Excel) {
		this.clazz = clazz;
		init(inputStream, is03Excel);
	}

	private ImportExcel(File file, Class<T> clazz) {
		this.clazz = clazz;
		try (FileInputStream inputStream = new FileInputStream(file)) {
			init(inputStream, file.getName().matches("^.+\\.(?i)(xls)$"));
		} catch (Exception e) {
			throw new ExcelImportException("创建ImportExcel失败!", e);
		}
	}

	private void init(InputStream inputStream, boolean is03Excel) {
		try {
			// 读完自动关闭流
			workbook = is03Excel ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
			sheet = workbook.getSheetAt(0);
			title = sheet.getRow(0);

			List<Field> fieldList = ExcelHelper.getDeclaredFieldsOrder(clazz);
			for (Field field : fieldList) {
				this.list.add(field);
			}
		} catch (Exception e) {
			throw new ExcelImportException("创建ImportExcel失败!", e);
		}
	}

	public static <U> List<U> syncImport(InputStream inputStream, Class<U> clazz, boolean is03Excel) {
		ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, is03Excel);
		return importExcel.parse();
	}

	public static <U> List<U> syncImport(File file, Class<U> clazz) {
		ImportExcel<U> importExcel = new ImportExcel<>(file, clazz);
		return importExcel.parse();
	}

	public static <U> Future<List<U>> asyncImport(ExecutorService executorService, InputStream inputStream,
												  Class<U> clazz, boolean is03Excel) {
		FutureTask<List<U>> futureTask = new FutureTask<>(() -> {
			ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, is03Excel);
			return importExcel.parse();
		});
		executorService.submit(futureTask);
		return futureTask;
	}

	public static <U> Future<List<U>> asyncImport(ExecutorService executorService, File file, Class<U> clazz) {
		FutureTask<List<U>> futureTask = new FutureTask<>(() -> {
			ImportExcel<U> importExcel = new ImportExcel<>(file, clazz);
			return importExcel.parse();
		});
		executorService.submit(futureTask);
		return futureTask;
	}


	private List<T> parse() {
		List<T> list = new LinkedList<>();
		Itr itr = (Itr) iterator();

		while (itr.hasNext()) {
			T t = itr.next();
			if (t != null) {
				list.add(t);
			}
		}
		return list;
	}

	private DataWrapper<T> parseOneRow(Row row) {
		if (row == null) {
			return new DataWrapper<>(null, true);
		}

		T instance = null;
		boolean flag = false;
		try {
			instance = clazz.newInstance();
			for (int j = 0; j < row.getLastCellNum(); j++) {
				Cell titleCell = title.getCell(j);
				if (titleCell == null) {
					continue;
				}
				if (j >= list.size()) {
					break;
				}

				// 获取字段名称
				String fieldName = list.get(j).getName();

				// 得到set函数
				String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

				// 获取方法, 但是不知道参数类型, 一定要传入null
				Method method = ClassUtils.getMethodByName(clazz, methodName, null);

				// 得到表格的值
				String fieldValue = this.cellValueToString(row.getCell(j));

				// 赋值
				if (!StringUtils.isEmpty(fieldValue)) {
					setFieldValue(instance, method, fieldValue);
					flag = true;
				}
			}
		} catch (Exception e) {
			throw new ExcelImportException("导入数据出错!", e);
		}
		return flag ? new DataWrapper<>(instance, false) : new DataWrapper<>(null, true);
	}

	private String cellValueToString(Cell cell) {
		if (cell == null) {
			return null;
		}
		switch (cell.getCellTypeEnum()) {
			case NUMERIC: // 数字
				return String.valueOf(cell.getNumericCellValue());
			case STRING: // 字符串
				return cell.getStringCellValue();
			case BOOLEAN: // Boolean
				return String.valueOf(cell.getBooleanCellValue());
			default:
				return null;
		}
	}

	private void setFieldValue(T t, Method method, String value) {
		try {
			Class<?> fieldClass = method.getParameterTypes()[0];
			if (fieldClass == String.class) {
				ReflexUtils.invokeMethod(method, t, value);
				return;
			}

			if (ConverterSupport.isSupport(String.class, fieldClass)) {
				Optional<?> optional = ConverterSupport.convert(String.class, fieldClass, value);
				Object arg = optional.orElse(null);
				ReflexUtils.invokeMethod(method, t, arg);
				return;
			}

			throw new RuntimeException("不支持String转换成" + fieldClass.getName());
		} catch (Exception e) {
			throw new ExcelImportException("设置字段值出错!", e);
		}
	}


	private Iterator<T> iterator() {
		return new Itr();
	}

	private class Itr implements Iterator<T> {

		int cursor = 1;
		int lastRet = -1;
		Queue<T> queue = new LinkedList<>();

		@Override
		public boolean hasNext() {
			if (queue.size() > 0) {
				return true;
			}
			return peek();
		}

		@Override
		public T next() {
			if (cursor > sheet.getLastRowNum())
				throw new NoSuchElementException();
			if (queue.size() > 0 || peek()) {
				lastRet = cursor;
				cursor++;
				return queue.poll();
			}
			throw new NoSuchElementException();
		}

		public boolean peek() {
			while (cursor <= sheet.getLastRowNum()) {
				DataWrapper<T> dataWrapper = parseOneRow(sheet.getRow(cursor));
				if (dataWrapper.t == null && dataWrapper.skip == true) {
					lastRet = cursor;
					cursor++;
				} else {
					queue.add(dataWrapper.t);
					return true;
				}
			}
			return false;
		}

		@Override
		public void remove() {
			throw new ExcelImportException("不支持remove操作");
		}

		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			while (hasNext()) {
				T t = next();
				if (t != null) {
					action.accept(t);
				}
			}
		}
	}

	private class DataWrapper<T> {
		protected T t;
		protected boolean skip; // skip:true 表示是一个空行

		public DataWrapper(T t, boolean skip) {
			this.t = t;
			this.skip = skip;
		}
	}

	public static void main(String[] args) {
		List<Name> names = syncImport(new File("/tmp/export/myname.xls"), Name.class);

		System.out.println(names);
	}

	public static class Name {
		@Excel(order = 1)
		private String name;
		@Excel(order = 2)
		private Integer age;

		public Name() {
		}

		public Name(String name, Integer age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return "Name{" +
					"name='" + name + '\'' +
					", age=" + age +
					'}';
		}
	}

}
