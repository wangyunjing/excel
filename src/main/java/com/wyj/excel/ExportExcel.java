package com.wyj.excel;

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

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 导出Excel
 * 导出的数据类必须有get方法
 */
public class ExportExcel {

    private static final Logger log = LoggerFactory.getLogger(ExportExcel.class);

    private static final Function<OutputStream, Boolean> NOT_CLOSE_STREAM = stream -> true;
    private static final Function<OutputStream, Boolean> CLOSE_STREAM = ExportExcel::close;

    private Workbook workbook;

    private Sheet sheet;

    // 导出的数据
    private List dataList;

    // 插入顺序
    private Map<ExcelField, Excel> map = new LinkedHashMap<>();

    private ExcelOptions options;

    private OutputStream outputStream;

    /**
     * 关闭输出流
     */
    private Function<OutputStream, Boolean> closeOutputStreamFunction;

    private ExportExcel(File file, Class clazz, List dataList, ExcelOptions options) {
        this.closeOutputStreamFunction = CLOSE_STREAM;

        Boolean is03Excel = file.getName().matches("^.+\\.(?i)(xls)$");

        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            init(outputStream, is03Excel, clazz, dataList, options);
        } catch (Exception e) {
            close(outputStream);
            close(this.workbook);
            throw new ExcelExportException(e);
        }
    }

    private ExportExcel(OutputStream outputStream, Class clazz, List dataList, ExcelOptions options) {
        this.closeOutputStreamFunction = NOT_CLOSE_STREAM;
        init(outputStream, null, clazz, dataList, options);
    }

    private void init(OutputStream outputStream, Boolean is03Excel, Class clazz, List dataList, ExcelOptions options) {
        Assert.notNull(outputStream, "outputStream must not be null");
        Assert.notNull(clazz, "clazz must not be null");
        Assert.notNull(dataList, "dataList must not be null");
        Assert.notNull(options, "options must not be null");

        this.outputStream = outputStream;
        this.dataList = dataList;
        this.options = options;
        is03Excel = is03Excel == null ? options.getIs03Excel() : is03Excel;

        List<ExcelField> fieldList = ExcelHelper.getExcelFields(clazz, options.getConverterService());
        for (ExcelField field : fieldList) {
            Excel excel = field.getAnnotation(Excel.class);
            map.put(field, excel);
        }

        this.workbook = is03Excel ? new HSSFWorkbook() : new XSSFWorkbook();
        if (options.getSheetName() == null) {
            this.sheet = workbook.createSheet();
        } else {
            this.sheet = workbook.createSheet(options.getSheetName());
        }
    }

    public static void execute(File file, Class clazz, List dataList, ExcelOptions options) {
        ExportExcel exportExcel = new ExportExcel(file, clazz, dataList, options);
        exportExcel.export();
    }

    public static void execute(File file, Class clazz, List dataList) {
        ExportExcel exportExcel = new ExportExcel(file, clazz, dataList, ExcelOptions.getDefaultInstance());
        exportExcel.export();
    }

    /**
     * @param outputStream 调用方决定何时关闭
     * @param clazz
     * @param dataList
     * @param options
     */
    public static void execute(OutputStream outputStream, Class clazz, List dataList, ExcelOptions options) {
        ExportExcel exportExcel = new ExportExcel(outputStream, clazz, dataList, options);
        exportExcel.export();
    }

    public static void execute(OutputStream outputStream, Class clazz, List dataList) {
        ExportExcel exportExcel = new ExportExcel(outputStream, clazz, dataList, ExcelOptions.getDefaultInstance());
        exportExcel.export();
    }

    private void export() {
        try {
            createTitle();
            createContent();
            workbook.write(outputStream);
        } catch (Exception e) {
            throw new ExcelExportException("excel export error!", e);
        } finally {
            close(workbook);
            closeOutputStreamFunction.apply(outputStream);
        }
    }

    private void createTitle() {
        createRow(options.getTitleRow(), (entry, row, colNum) -> {
            Excel excel = entry.getValue();
            createCell(row, colNum, excel.name());
        });
    }

    private void createContent() {
        for (int i = 0; i < dataList.size(); i++) {
            final Object data = dataList.get(i);
            if (!options.getPredicate().test(data)) {
                continue;
            }
            createRow(options.getTitleRow() + i + 1, (entry, row, colNum) -> {
                ExcelField field = entry.getKey();
                String fieldValue = field.get(data, String.class);
                createCell(row, colNum, fieldValue);
            });
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
        if (value == null) {
            return;
        }
        if (options.getCellValueTrim()) {
            value = value.trim();
        }
        Cell cell = row.createCell(colNum);
        cell.setCellValue(value);
    }

    interface IteratorMap<K, V> {
        void iterator(Map.Entry<K, V> entry, Row row, int colNum);
    }

    private static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.debug("关闭流报错！", e);
                return false;
            }
        }
        return true;
    }
}
