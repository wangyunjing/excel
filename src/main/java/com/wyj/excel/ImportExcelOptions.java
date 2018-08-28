package com.wyj.excel;

import com.wyj.excel.convert.Converter;
import com.wyj.excel.convert.ConverterService;

import java.util.function.Predicate;

public class ImportExcelOptions {

    // 默认sheet所在的位置
    private static final Integer DEFAULT_SHEET_IDX = 0;
    // 默认标题所在的行
    private static final Integer DEFAULT_TITLE_ROW = 0;

    private static final Boolean DEFAULT_Is03Excel = false;

    private static final Predicate DEFAULT_PREDICATE = obj -> true;

    private static final ConverterService DEFAULT_CONVERTER_SERVICE = ConverterService.create();

    // 是否对表格内容进行trim
    private Boolean cellValueTrim;

    // 是否过滤空白行（所有的内容都是null或者空字符串""）
    private Boolean filterBlankLine;

    // sheet 所在的位置
    private Integer sheetIdx;

    // 标题所在的行
    private Integer titleRow;

    // 是否是xsl格式的excel
    private Boolean is03Excel;

    // 转换器
    private ConverterService converterService;

    // 过滤器
    private Predicate predicate;

    private ImportExcelOptions() {
    }

    private ImportExcelOptions(ImportExcelOptions options) {
        this.cellValueTrim = options.cellValueTrim == null ? Boolean.TRUE : options.cellValueTrim;
        this.filterBlankLine = options.filterBlankLine == null ? Boolean.TRUE : options.filterBlankLine;
        this.sheetIdx = options.sheetIdx == null ? DEFAULT_SHEET_IDX : options.sheetIdx;
        this.titleRow = options.titleRow == null ? DEFAULT_TITLE_ROW : options.titleRow;
        this.is03Excel = options.is03Excel == null ? DEFAULT_Is03Excel : options.is03Excel;
        this.converterService = options.converterService == null ? DEFAULT_CONVERTER_SERVICE : ConverterService.create(options.converterService);
        this.predicate = options.predicate == null ? DEFAULT_PREDICATE : options.predicate;
    }

    public Boolean getCellValueTrim() {
        return cellValueTrim;
    }

    public Boolean getFilterBlankLine() {
        return filterBlankLine;
    }

    public Integer getSheetIdx() {
        return sheetIdx;
    }

    public Integer getTitleRow() {
        return titleRow;
    }

    public Boolean getIs03Excel() {
        return is03Excel;
    }

    public ConverterService getConverterService() {
        return converterService;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public static class Builder<T> {

        private ImportExcelOptions options;

        private Builder() {
            options = new ImportExcelOptions();
        }

        public static <T> Builder<T> create() {
            return new Builder<>();
        }

        public Builder setCellValueTrim(Boolean cellValueTrim) {
            options.cellValueTrim = cellValueTrim;
            return this;
        }

        public Builder setFilterBlankLine(Boolean filterBlankLine) {
            options.filterBlankLine = filterBlankLine;
            return this;
        }

        public Builder setSheetIdx(Integer sheetIdx) {
            options.sheetIdx = sheetIdx;
            return this;
        }

        public Builder setIs03Excel(Boolean is03Excel) {
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
            options.converterService.addConvert(sourceClass, targetClass, converter);
            return this;
        }

        public ImportExcelOptions build() {
            return new ImportExcelOptions(options);
        }
    }

}
