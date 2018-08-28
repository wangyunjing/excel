package com.wyj.excel;

import com.wyj.excel.convert.ConverterService;
import com.wyj.excel.annotation.Excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wyj on 17-12-21.
 */
public class ExcelFieldBuilder {
    private Excel excel;
    private ConverterService converterService;
    private List<Field> list = new ArrayList<>();
    ;

    private ExcelFieldBuilder() {
    }

    private ExcelFieldBuilder(List<Field> list) {
        this.list.addAll(list);
    }

    public static ExcelFieldBuilder newInstance() {
        return new ExcelFieldBuilder();
    }

    public static ExcelFieldBuilder newInstance(List<Field> list) {
        return new ExcelFieldBuilder(list);
    }

    public static ExcelFieldBuilder newInstance(Field... fields) {
        return new ExcelFieldBuilder(Arrays.asList(fields));
    }

    public ExcelFieldBuilder addField(Field field) {
        list.add(field);
        return this;
    }

    public ExcelFieldBuilder addField(Field... fields) {
        if (fields == null || fields.length == 0) {
            return this;
        }
        list.addAll(Arrays.asList(fields));
        return this;
    }

    public ExcelFieldBuilder addField(List<Field> list) {
        if (list == null || list.size() == 0) {
            return this;
        }
        list.addAll(list);
        return this;
    }

    public ExcelFieldBuilder setConverterService(ConverterService converterService) {
        this.converterService = converterService;
        return this;
    }

    public ExcelFieldBuilder setExcel(Excel excel) {
        this.excel = excel;
        return this;
    }

    public ExcelField build() {
        if (list == null || list.size() == 0) {
            return null;
        }
        return new ExcelField(excel, converterService == null ? ConverterService.create() : converterService,
                list.toArray(new Field[list.size()]));
    }

}
