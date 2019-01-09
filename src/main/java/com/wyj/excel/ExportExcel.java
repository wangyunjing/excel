package com.wyj.excel;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.exception.ExcelExportException;
import com.wyj.excel.util.Assert;
import com.wyj.excel.util.ExceptionUtils;
import com.wyj.excel.util.FileUtils;
import com.wyj.excel.util.Try;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 导出Excel
 * 导出的数据类必须有get方法
 */
public class ExportExcel implements Closeable {

    private static final Consumer<OutputStream> NOT_CLOSE_STREAM = stream -> {
    };
    private static final Consumer<OutputStream> CLOSE_STREAM = ExportExcel::closeStream;




    private Workbook workbook;

    private Sheet sheet;

    /**
     * 数据
     */
    private List dataList;

    /**
     * 数据类型
     */
    private Class clazz;

    /**
     * 插入顺序
     */
    private Map<ExcelField, Excel> map = new LinkedHashMap<>();

    /**
     * 选项
     */
    private ExportExcelOptions options;

    /**
     * 输出流
     */
    private OutputStream outputStream;

    /**
     * 导出完数据的后处理
     */
    private Runnable postProcessing = ()->{};

    /**
     * 关闭输出流
     */
    private Consumer<OutputStream> closeOutputStreamFunction;

    private ExportExcel(File file, Class clazz, List dataList, ExportExcelOptions options) {
        this.clazz = clazz;
        this.dataList = dataList;
        this.options = options;
        this.closeOutputStreamFunction = CLOSE_STREAM;
        if (clazz != null) {
            initMap();
        }
        File outFile = file;
        FileUtils.createParentFile(file);
        FileInputStream fileInputStream = null;
        try {
            Boolean is03Excel = file.getName().matches("^.+\\.(?i)(xls)$");
            is03Excel = is03Excel == null ? options.isIs03Excel() : is03Excel;
            if (file.exists() && options.isPreserveNodes()) {
                File tmpFile = FileUtils.createTmpFile(is03Excel ? "xls" : "xlsx");
                FileUtils.copyFile(file, tmpFile);
                outFile = tmpFile;
                postProcessing = () -> {
                    FileUtils.copyFile(tmpFile, file);
                    ExceptionUtils.engulf(() -> FileUtils.deleteFile(tmpFile));
                };
                fileInputStream = new FileInputStream(tmpFile);
                this.workbook = is03Excel ? new HSSFWorkbook(fileInputStream) : new XSSFWorkbook(fileInputStream);
            } else {
                this.workbook = is03Excel ? new HSSFWorkbook() : new XSSFWorkbook();
            }
            this.outputStream = new FileOutputStream(outFile);
        } catch (Exception e) {
            closeStream(fileInputStream);
            closeStream(outputStream);
            closeStream(this.workbook);
            throw new ExcelExportException(e);
        }
    }

    private ExportExcel(OutputStream outputStream, Class clazz, List dataList, ExportExcelOptions options) {
        try {
            this.clazz = clazz;
            this.dataList = dataList;
            this.options = options;
            this.closeOutputStreamFunction = NOT_CLOSE_STREAM;
            this.workbook = options.isIs03Excel() ? new HSSFWorkbook() : new XSSFWorkbook();
            this.outputStream = outputStream;
        } catch (Exception e) {
            closeStream(this.workbook);
            throw new ExcelExportException(e);
        }
    }

    private void init(Class clazz, List dataList, ExportExcelOptions options) {
        Assert.notNull(clazz, "clazz must not be null");
        Assert.notNull(dataList, "dataList must not be null");
        Assert.notNull(options, "options must not be null");

        this.dataList = dataList;
        this.options = options;
        if (this.clazz == null || this.clazz != clazz) {
            this.clazz = clazz;
            map.clear();
            initMap();
        }
    }

    private void initMap() {
        List<ExcelField> fieldList = ExcelHelper.getExcelFields(clazz, options.getConverterService());
        for (ExcelField field : fieldList) {
            Excel excel = field.getAnnotation(Excel.class);
            map.put(field, excel);
        }
    }

    public static void execute(File file, Class clazz, List dataList, ExportExcelOptions options) {
        ExportExcel exportExcel = new ExportExcel(file, clazz, dataList, options);
        exportExcel.export();
    }

    public static void execute(File file, Class clazz, List dataList) {
        ExportExcel exportExcel = new ExportExcel(file, clazz, dataList, ExportExcelOptions.getDefaultInstance());
        exportExcel.export();
    }

    /**
     * @param outputStream 调用方决定何时关闭
     */
    public static void execute(OutputStream outputStream, Class clazz, List dataList, ExportExcelOptions options) {
        ExportExcel exportExcel = new ExportExcel(outputStream, clazz, dataList, options);
        exportExcel.export();
    }

    /**
     * @param outputStream 调用方决定何时关闭
     */
    public static void execute(OutputStream outputStream, Class clazz, List dataList) {
        ExportExcel exportExcel = new ExportExcel(outputStream, clazz, dataList, ExportExcelOptions.getDefaultInstance());
        exportExcel.export();
    }

    public static ExportExcel build(File file) {
        ExportExcel exportExcel = new ExportExcel(file, null, null, ExportExcelOptions.getDefaultInstance());
        return exportExcel;
    }

    public static ExportExcel build(File file, ExportExcelOptions options) {
        ExportExcel exportExcel = new ExportExcel(file, null, null, options);
        return exportExcel;
    }

    /**
     * @param outputStream 调用方决定何时关闭
     */
    public static ExportExcel build(OutputStream outputStream) {
        ExportExcel exportExcel = new ExportExcel(outputStream, null, null, ExportExcelOptions.getDefaultInstance());
        return exportExcel;
    }

    /**
     * @param outputStream 调用方决定何时关闭
     */
    public static ExportExcel build(OutputStream outputStream, ExportExcelOptions options) {
        ExportExcel exportExcel = new ExportExcel(outputStream, null, null, options);
        return exportExcel;
    }

    private void export() {
        try {
            nextSheet(clazz, dataList);
            finish();
        } catch (Exception e) {
            throw new ExcelExportException("excel export error!", e);
        } finally {
            ExceptionUtils.engulf(Try.of(this::close));
        }
    }

    public void nextSheet(Class clazz, List dataList) throws Exception {
        nextSheet(clazz, dataList, options);
    }

    public void nextSheet(Class clazz, List dataList, ExportExcelOptions options) throws Exception {
        init(clazz, dataList, options);
        String sheetName = getSheetName();
        if (sheetName == null) {
            this.sheet = workbook.createSheet();
        } else {
            this.sheet = workbook.createSheet(sheetName);
        }
        createTitle();
        createContent();
    }

    private String getSheetName() {
        String sheetName = null;
        do {
            sheetName = options.getSheetName();
            if (sheetName == null) {
                return null;
            }
        } while (workbook.getSheet(sheetName) != null);
        return sheetName;
    }

    public void finish() throws IOException {
        workbook.write(outputStream);
        postProcessing.run();
    }

    @Override
    public void close() throws IOException {
        closeStream(workbook);
        closeOutputStreamFunction.accept(outputStream);
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
        if (options.isCellValueTrim()) {
            value = value.trim();
        }
        Cell cell = row.createCell(colNum);
        cell.setCellValue(value);
    }

    interface IteratorMap<K, V> {
        void iterator(Map.Entry<K, V> entry, Row row, int colNum);
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                //
            }
        }
    }
}
