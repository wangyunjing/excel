package com.wyj.excel;

import com.wyj.excel.annotation.Excel;
import com.wyj.excel.exception.ExcelImportException;
import com.wyj.excel.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
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

    private ImportExcelOptions options;

    private ImportExcel(InputStream inputStream, Class<T> clazz, ImportExcelOptions options) {
        this.clazz = clazz;
        init(inputStream, options.getIs03Excel(), options);
    }

    private ImportExcel(File file, Class<T> clazz, ImportExcelOptions options) {
        Assert.notNull(file, "file must not be null");
        this.clazz = clazz;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Boolean is03Excel = file.getName().matches("^.+\\.(?i)(xls)$");
            init(inputStream, is03Excel, options);
        } catch (ExcelImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelImportException("创建ImportExcel失败!", e);
        }
    }

    private void init(InputStream inputStream, Boolean is03Excel, ImportExcelOptions options) {
        Assert.notNull(inputStream, "inputStream must not be null");
        Assert.notNull(options, "options must not be null");
        try {
            is03Excel = is03Excel == null ? options.getIs03Excel() : is03Excel;
            this.workbook = is03Excel ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
            if (options.getSheetName() == null) {
                this.sheet = workbook.getSheetAt(options.getSheetIdx());
            } else if (options.getSheetIdx() == null) {
                this.sheet = workbook.getSheet(options.getSheetName());
            } else {
                this.sheet = workbook.getSheetAt(options.getSheetIdx());
                Assert.isTrue(options.getSheetName().equals(this.sheet.getSheetName()), "sheet don't exist");
            }
            this.title = sheet.getRow(options.getTitleRow());

            this.list = ExcelHelper.getExcelFields(clazz, options.getConverterService());

            this.options = options;
        } catch (Exception e) {
            throw new ExcelImportException("创建ImportExcel失败!", e);
        }
    }

    /**
     * @param inputStream 调用方决定何时close
     * @param clazz
     * @param options
     * @param <U>
     * @return
     */
    public static <U> List<U> execute(InputStream inputStream, Class<U> clazz, ImportExcelOptions options) {
        ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, options);
        return importExcel.parse();
    }

    public static <U> List<U> execute(InputStream inputStream, Class<U> clazz) {
        ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, ImportExcelOptions.getDefaultInstance());
        return importExcel.parse();
    }

    /**
     * @param file    文件类型扩展后缀优先级大于options设置的Excel类型
     * @param clazz
     * @param options
     * @param <U>
     * @return
     */
    public static <U> List<U> execute(File file, Class<U> clazz, ImportExcelOptions options) {
        ImportExcel<U> importExcel = new ImportExcel<>(file, clazz, options);
        return importExcel.parse();
    }

    public static <U> List<U> execute(File file, Class<U> clazz) {
        ImportExcel<U> importExcel = new ImportExcel<>(file, clazz, ImportExcelOptions.getDefaultInstance());
        return importExcel.parse();
    }

    private List<T> parse() {
        // 解析标题
        for (int i = 0; i < title.getLastCellNum(); i++) {
            if (titleList == null) {
                titleList = new LinkedList<>();
            }
            String titleValue = cellValueToString(title.getCell(i));
            titleValue = titleValue == null ? "" : titleValue;
            titleValue = titleValue.trim();
            titleList.add(titleValue);
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
        // false:跳过该实例； true:保留该实例
        boolean keepInstance = !options.getFilterBlankLine();
        // 列
        int j = 0;
        try {
            instance = clazz.newInstance();
            int excelFieldCol = -1;
            Map<Integer, Integer> colMap = new HashMap<>(list.size());
            List<Pair<Integer, String>> allValues = new ArrayList<>(list.size());
            for (j = 0; j < row.getLastCellNum(); j++) {
                // 防止下标越界以及提前跳出循环
                if (excelFieldCol >= list.size() - 1) {
                    break;
                }
                // 判断是否@Excel#name和title是否相同
                String titleName = titleList.get(j);
                titleName = titleName == null ? "" : titleName;
                String excelName = list.get(excelFieldCol + 1).getAnnotation(Excel.class).name();
                if ("".equals(excelName) || excelName.equals(titleName)) {
                    excelFieldCol++;
                    colMap.put(excelFieldCol, j);
                } else {
                    continue;
                }
                // 得到表格的值
                String fieldValue = this.cellValueToString(row.getCell(j));
                allValues.add(Pair.of(excelFieldCol, fieldValue));
                // 过滤空行
                if (!StringUtils.isEmpty(fieldValue)) {
                    keepInstance = true;
                }
            }
            if (keepInstance == true) {
                for (Pair<Integer, String> pair : allValues) {
                    j = colMap.get(pair.getLeft());
                    list.get(pair.getLeft()).set(instance, pair.getValue());
                }
            }
            // 判断是否需要过滤该实例
            keepInstance = keepInstance == true ? options.getPredicate().test(instance) : false;
        } catch (Exception e) {
            throw new ExcelImportException("导入Excel第" + (row.getRowNum() + 1) + "行" + (j + 1) + "列数据时发生异常!", e);
        }
        return keepInstance ? new DataWrapper<>(instance, false) : new DataWrapper<>(instance, true);
    }

    private String cellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellTypeEnum()) {
            case NUMERIC: // 数字 防止科学计数
                String bigDecimalString = new BigDecimal(String.valueOf(cell.getNumericCellValue())).toPlainString();
                int idx = bigDecimalString.lastIndexOf(".");
                if (idx == -1) {
                    return bigDecimalString;
                }
                for (int i = idx + 1; i < bigDecimalString.length(); i++) {
                    if (bigDecimalString.charAt(i) != '0') {
                        return bigDecimalString;
                    }
                }
                return bigDecimalString.substring(0, idx);
            case STRING: // 字符串
                String cellValue = cell.getStringCellValue();
                if (cellValue != null && options.getCellValueTrim()) {
                    cellValue = cellValue.trim();
                }
                return cellValue;
            case BOOLEAN: // Boolean
                return String.valueOf(cell.getBooleanCellValue());
            default:
                throw new ExcelImportException("不支持的Excel数据类型！" + cell.getCellTypeEnum());
        }
    }

    private Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<T> {

        int cursor = options.getTitleRow() + 1;
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
                if (dataWrapper.skip == true) {
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
        protected boolean skip; // skip:true 表示该数据需要跳过

        public DataWrapper(T t, boolean skip) {
            this.t = t;
            this.skip = skip;
        }
    }
}
