package com.wyj.excel;

import com.wyj.excel.convert.Converter;
import com.wyj.excel.convert.ConverterService;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class ExportExcelOptions {


    private static final Predicate DEFAULT_PREDICATE = obj -> true;

    private static class ExcelOptionsHolder {
        public static final ExportExcelOptions DEFAULT_OPTIONS = Builder.create().build();
    }

    /**
     * 是否对表格内容进行trim
     */
    private boolean cellValueTrim = true;


    /**
     * sheet名称
     */
    private String sheetName;

    /**
     * 当preserveNodes=true时，sheetName需要添加后缀防止冲突
     */
    private int sheetNameSuffix = 1;

    /**
     * 是否避免sheet名称冲突
     */
    private boolean avoidSheetNameConflict = false;

    /**
     * 导出Excel时，如果文件存在，是否保留文件之前的内容
     */
    private boolean preserveNodes = false;

    /**
     * 标题所在的行
     */
    private int titleRow = 0;

    /**
     * 是否是xsl格式的excel
     */
    private boolean is03Excel = false;

    /**
     * 转换器
     */
    private ConverterService converterService;

    /**
     * 过滤器
     */
    private Predicate predicate;

    private ExportExcelOptions() {
    }

    private ExportExcelOptions(ExportExcelOptions options) {
        this.cellValueTrim = options.cellValueTrim;
        this.sheetName = options.sheetName;
        this.avoidSheetNameConflict = options.avoidSheetNameConflict;
        this.preserveNodes =  options.preserveNodes;
        this.titleRow = options.titleRow;
        this.is03Excel = options.is03Excel;
        this.converterService = options.converterService == null ? ConverterService.getDefaultInstance() : ConverterService.create(options.converterService);
        this.predicate = options.predicate == null ? DEFAULT_PREDICATE : options.predicate;
    }

    public static ExportExcelOptions getDefaultInstance() {
        return ExcelOptionsHolder.DEFAULT_OPTIONS;
    }

    public boolean isCellValueTrim() {
        return cellValueTrim;
    }

    public String getSheetName() {
        if (sheetName == null || !avoidSheetNameConflict) {
            return sheetName;
        }
        return sheetName + sheetNameSuffix++;
    }

    public boolean isPreserveNodes() {
        return preserveNodes;
    }

    public int getTitleRow() {
        return titleRow;
    }

    public boolean isIs03Excel() {
        return is03Excel;
    }

    public ConverterService getConverterService() {
        return converterService;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public static class Builder<T> {

        private ExportExcelOptions options;

        private Builder() {
            options = new ExportExcelOptions();
        }

        public static <T> Builder<T> create() {
            return new Builder<>();
        }

        public Builder setCellValueTrim(boolean cellValueTrim) {
            options.cellValueTrim = cellValueTrim;
            return this;
        }

        public Builder setTitleRow(int titleRow) {
            options.titleRow = titleRow;
            return this;
        }

        public Builder setIs03Excel(boolean is03Excel) {
            options.is03Excel = is03Excel;
            return this;
        }

        public Builder setPredicate(Predicate<T> predicate) {
            options.predicate = predicate;
            return this;
        }

        public Builder setConverterService(ConverterService converterService) {
            options.converterService = converterService;
            return this;
        }

        public Builder addConverter(Class sourceClass, Class targetClass, Converter<?, ?> converter) {
            if (options.converterService == null) {
                options.converterService = ConverterService.create();
            }
            options.converterService.addConverter(sourceClass, targetClass, converter);
            return this;
        }

        public Builder setSheetName(String sheetName) {
            options.sheetName = sheetName;
            return this;
        }

        public Builder setPreserveNodes(boolean preserveNodes) {
            options.preserveNodes = preserveNodes;
            return this;
        }

        public Builder setAvoidSheetNameConflict(boolean avoidSheetNameConflict) {
            options.avoidSheetNameConflict = avoidSheetNameConflict;
            return this;
        }
        public ExportExcelOptions build() {
            return new ExportExcelOptions(options);
        }
    }
}
