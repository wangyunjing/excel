package com.wyj.excel;

import com.wyj.excel.convert.ConverterService;
import com.wyj.excel.annotation.Excel;
import com.wyj.excel.exception.ExcelExportException;
import com.wyj.excel.util.Assert;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 导出Excel
 * 导出的数据类必须有get方法
 */
public class ExportExcel {

    private static final Logger log = LoggerFactory.getLogger(ExportExcel.class);

    private File file;

    private Workbook workbook;

    private Sheet sheet;

    private boolean is03Excel;

    private Class clazz;

    // 导出的数据
    private List dataList;

    // 插入顺序
    private Map<ExcelField, Excel> map = new LinkedHashMap<>();

    private ConverterService converterService;

    private ExportExcel(File file, Class clazz, List dataList) {
        Assert.notNull(file, "file must not be null");
        Assert.notNull(clazz, "clazz must not be null");
        Assert.notNull(dataList, "dataList must not be null");

        this.file = file;
        this.dataList = dataList;
        this.clazz = clazz;
        // TODO: 2018/8/28 设置Options
        this.converterService = ConverterService.create();
        init();
    }

    private void init() {
        List<ExcelField> fieldList = ExcelHelper.getExcelFields(clazz, converterService);
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
        String convert = convert(excelField.get(t));

        return convert == null ? "" : convert;
    }

    private String convert(Object object) {
        if (object == null) {
            return null;
        }
        Class sourceClass = object.getClass();
        Class targetClass = String.class;

        if (sourceClass == targetClass) {
            return (String) object;
        }
        try {
            return (String) converterService.convert(sourceClass, targetClass, object);
        } catch (Exception e) {
            log.warn("类型转换出错！ msg={}", e.getMessage());
        }
        return object.toString();
    }

    interface IteratorMap<K, V> {
        void iterator(Map.Entry<K, V> entry, Row row, int colNum);
    }

}
