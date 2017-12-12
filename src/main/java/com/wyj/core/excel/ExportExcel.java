package com.wyj.core.excel;

import com.wyj.core.convert.ConverterSupport;
import com.wyj.core.excel.annotation.Excel;
import com.wyj.core.excel.exception.ExcelExportException;
import com.wyj.core.util.Assert;
import com.wyj.core.util.ClassUtils;
import com.wyj.core.util.ReflexUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * 导出Excel
 * 导出的数据类必须有get方法
 */
public class ExportExcel {

	private File file;

	private Workbook workbook;

	private Sheet sheet;

	private boolean is03Excel;

	private Class clazz;

	// 导出的数据
	private List dataList;

	// 插入顺序
	private Map<Field, Excel> map = new LinkedHashMap<>();

	private ExportExcel(File file, Class clazz, List dataList) {
		Assert.notNull(clazz, "file must not be null");
		Assert.notNull(clazz, "clazz must not be null");
		Assert.notNull(dataList, "dataList must not be null");

		this.file = file;
		this.dataList = dataList;
		this.clazz = clazz;
		init();
	}

	private void init() {
		List<Field> fieldList = ExcelHelper.getDeclaredFieldsOrder(clazz);
		for (Field field : fieldList) {
			Excel excel = field.getAnnotation(Excel.class);
			map.put(field, excel);
		}

		this.is03Excel = file.getName().matches("^.+\\.(?i)(xls)$");
		this.workbook = is03Excel ? new HSSFWorkbook() : new XSSFWorkbook();
		this.sheet = workbook.createSheet();
	}

	public static void syncExport(File file, Class clazz, List dataList) {
		ExportExcel exportExcel = new ExportExcel(file, clazz, dataList);
		exportExcel.export();
	}

	public static CompletableFuture<Void> asyncExport(ExecutorService executorService, File file, Class clazz, List dataList) {
		CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
			ExportExcel exportExcel = new ExportExcel(file, clazz, dataList);
			exportExcel.export();
			return null;
		}, executorService);
		return completableFuture;
	}

	public static CompletableFuture<Void> asyncExport(File file, Class clazz, List dataList) {
		CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
			ExportExcel exportExcel = new ExportExcel(file, clazz, dataList);
			exportExcel.export();
			return null;
		});
		return completableFuture;
	}

	private void export() {
		try {
			createTitle();
			createContent();
			saveToFile();
		} catch (Throwable e) {
			throw new ExcelExportException("excel export error!", e);
		}
	}

	private void createTitle() {
		createRow(0, (entry, row, colNum) -> {
			Excel excel = entry.getValue();
			createCell(row, colNum, excel.name());
		});
	}

	private void createContent() {
		for (int i = 0; i < dataList.size(); i++) {
			final Object data = dataList.get(i);

			createRow(i + 1, (entry, row, colNum) -> {
				Field field = entry.getKey();
				String fieldValue = getFieldValue(data, field);
				createCell(row, colNum, fieldValue);
			});
		}
	}

	private void saveToFile() throws IOException {

		File parentFile = file.getParentFile();
		if (parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs();
		}

		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			workbook.write(outputStream);
		} finally {
			workbook.close();
		}
	}

	private void createRow(int rowNum, IteratorMap<Field, Excel> iteratorMap) {
		Row row = sheet.createRow(rowNum);
		int colNum = 0;
		Iterator<Map.Entry<Field, Excel>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			iteratorMap.iterator(iterator.next(), row, colNum++);
		}
	}

	private void createCell(Row row, int colNum, String value) {
		Cell cell = row.createCell(colNum);
		cell.setCellValue(value);
	}

	private String getFieldValue(Object t, Field field) {
		try {
			String fieldName = field.getName();
			String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

			// 获取没有参数的方法
			Method method = ClassUtils.getMethodByName(clazz, methodName);

			Object obj = ReflexUtils.invokeMethod(method, t);
			if (obj == null) {
				return "";
			}
			// 使用obj.getClass(), 而不使用field.getType(), 是因为有可能get方法返回的不是原类型
			if (ConverterSupport.isSupport(obj.getClass(), String.class)) {
				Optional<String> optional = ConverterSupport.convert(obj.getClass(), String.class, obj);
				return optional.orElse("");
			}
			return obj.toString();
		} catch (Exception e) {
			throw new ExcelExportException("获取字段值出错!", e);
		}
	}

	interface IteratorMap<K, V> {
		void iterator(Map.Entry<K, V> entry, Row row, int colNum);
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		List<Name> list = new ArrayList<>();
		list.add(new Name("w1", 1));
		list.add(new Name("w2", 2));
		list.add(new Name("w3", 3));
		list.add(new Name("w4", 4));
		list.add(new Name("w5", 5));
		list.add(new Name("w6", 6));
		Future<Void> future = ExportExcel.asyncExport(executorService, new File("/tmp/export/myname.xls"), Name.class, list);
		future.get();
		System.out.println("Main end!");
		executorService.shutdown();
		System.out.println("end!");
	}

	public static class Name {
		@Excel(name = "姓名", order = 1)
		private String name;
		@Excel(name = "姓名", order = 2)
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
	}
}
