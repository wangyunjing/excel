package com.example.excel;

import com.example.excel.annotation.Excel;
import com.example.excel.exception.ExcelImportException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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

	private List<ExcelField> list = new ArrayList<>();
	private List<String> titleList;

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

			list = ExcelHelper.getExcelFields(clazz);
		} catch (Exception e) {
			throw new ExcelImportException("创建ImportExcel失败!", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public static <U> CompletableFuture<List<U>> asyncImport(ExecutorService executorService, InputStream inputStream,
															 Class<U> clazz, boolean is03Excel) {
		CompletableFuture<List<U>> completableFuture = CompletableFuture.supplyAsync(() -> {
			ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, is03Excel);
			return importExcel.parse();
		}, executorService);
		return completableFuture;
	}

	public static <U> CompletableFuture<List<U>> asyncImport(ExecutorService executorService, File file, Class<U> clazz) {

		CompletableFuture<List<U>> completableFuture = CompletableFuture.supplyAsync(() -> {
			ImportExcel<U> importExcel = new ImportExcel<>(file, clazz);
			return importExcel.parse();
		}, executorService);
		return completableFuture;
	}

	public static <U> CompletableFuture<List<U>> asyncImport(InputStream inputStream, Class<U> clazz, boolean is03Excel) {
		CompletableFuture<List<U>> completableFuture = CompletableFuture.supplyAsync(() -> {
			ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, is03Excel);
			return importExcel.parse();
		});
		return completableFuture;
	}

	public static <U> CompletableFuture<List<U>> asyncImport(File file, Class<U> clazz) {
		CompletableFuture<List<U>> completableFuture = CompletableFuture.supplyAsync(() -> {
			ImportExcel<U> importExcel = new ImportExcel<>(file, clazz);
			return importExcel.parse();
		});
		return completableFuture;
	}


	private List<T> parse() {
		// 解析标题
		for (int i = 0; i < title.getLastCellNum(); i++) {
			if (titleList == null) {
				titleList = new LinkedList<>();
			}
			titleList.add(cellValueToString(title.getCell(i)));
		}

		// 解析数据
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
			int excelFieldCol = -1;
			for (int j = 0; j < row.getLastCellNum(); j++) {

				// 防止下标越界以及提前跳出循环
				if (excelFieldCol >= list.size() - 1) {
					break;
				}

				// 判断是否@Excel#name和title是否相同
				String titleName = titleList.get(j);
				String excelName = list.get(excelFieldCol + 1).getAnnotation(Excel.class).name();
				if ("".equals(excelName) || ("".equals(excelName) && titleName == null) ||
						excelName.equals(titleName)) {
					excelFieldCol++;
				} else {
					continue;
				}

				// 得到表格的值
				String fieldValue = this.cellValueToString(row.getCell(j));
				if (!StringUtils.isEmpty(fieldValue)) {
					list.get(excelFieldCol).set(instance, fieldValue);
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
				return new BigDecimal(String.valueOf(cell.getNumericCellValue())).toPlainString();
			case STRING: // 字符串
				return cell.getStringCellValue();
			case BOOLEAN: // Boolean
				return String.valueOf(cell.getBooleanCellValue());
			default:
				return null;
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
}
