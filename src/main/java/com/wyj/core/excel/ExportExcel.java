package com.wyj.core.excel;

import com.wyj.core.excel.annotation.Excel;
import com.wyj.core.excel.exception.ExcelExportException;
import com.wyj.core.util.Assert;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
	private Map<ExcelField, Excel> map = new LinkedHashMap<>();

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
		List<ExcelField> fieldList = ExcelHelper.getExcelFields(clazz);
		for (ExcelField field : fieldList) {
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
		} catch (Exception e) {
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
				ExcelField field = entry.getKey();
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

	private void createRow(int rowNum, IteratorMap<ExcelField, Excel> iteratorMap) {
		Row row = sheet.createRow(rowNum);
		int colNum = 0;
		Iterator<Map.Entry<ExcelField, Excel>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			iteratorMap.iterator(iterator.next(), row, colNum++);
		}
	}

	private void createCell(Row row, int colNum, String value) {
		Cell cell = row.createCell(colNum);
		cell.setCellValue(value);
	}

	private String getFieldValue(Object t, ExcelField excelField) {
		String s = excelField.get(t, String.class);
		return s == null ? "" : s;
	}

	interface IteratorMap<K, V> {
		void iterator(Map.Entry<K, V> entry, Row row, int colNum);
	}

}
