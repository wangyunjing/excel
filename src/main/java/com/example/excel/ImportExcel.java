package com.example.excel;

import com.example.excel.annotation.Excel;
import com.example.excel.exception.ExcelImportException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(ImportExcel.class);

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
        try {
            is03Excel = is03Excel == null ? options.getIs03Excel() : is03Excel;
            this.workbook = is03Excel ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
            this.sheet = workbook.getSheetAt(options.getSheetIdx());
            this.title = sheet.getRow(options.getTitleRow());

            this.list = ExcelHelper.getExcelFields(clazz, options.getConverterService());

            this.options = options;
        } catch (Exception e) {
            throw new ExcelImportException("创建ImportExcel失败!", e);
        }
    }

    public static <U> List<U> execute(InputStream inputStream, Class<U> clazz, ImportExcelOptions options) {
        ImportExcel<U> importExcel = new ImportExcel<>(inputStream, clazz, options);
        return importExcel.parse();
    }

    public static <U> List<U> execute(File file, Class<U> clazz, ImportExcelOptions options) {
        ImportExcel<U> importExcel = new ImportExcel<>(file, clazz, options);
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
        boolean flag = false;
        try {
            instance = clazz.newInstance();
            int excelFieldCol = -1;
            List<Pair<Integer, String>> allVlues = new ArrayList<>(list.size());
            for (int j = 0; j < row.getLastCellNum(); j++) {
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
                } else {
                    continue;
                }

                // 得到表格的值
                String fieldValue = this.cellValueToString(row.getCell(j));
                allVlues.add(Pair.of(excelFieldCol, fieldValue));
                // 过滤空行
                flag = !options.getFilterBlankLine();
                if (options.getFilterBlankLine() && !StringUtils.isEmpty(fieldValue)) {
                    flag = true;
                }
            }
            if (flag == true) {
                for (Pair<Integer, String> pair : allVlues) {
                    list.get(pair.getLeft()).set(instance, pair.getValue());
                }
            }
            // 判断是否需要过滤该实例
            flag = flag == true ? options.getPredicate().test(instance) : false;
        } catch (Exception e) {
            throw new ExcelImportException("导入数据出错!", e);
        }
        return flag ? new DataWrapper<>(instance, false) : new DataWrapper<>(instance, true);
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
                return null;
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
